package com.github.sebastiant.jchord.overlay;

import com.github.sebastiant.jchord.network.Address;
import com.github.sebastiant.jchord.network.ConcreteObserver;
import com.github.sebastiant.jchord.network.MessageSender;
import com.github.sebastiant.jchord.network.events.ConnectionRefusedEvent;
import com.github.sebastiant.jchord.network.events.ControlEvent;
import com.github.sebastiant.jchord.network.events.Message;
import com.github.sebastiant.jchord.overlay.datastorage.DataStore;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Node implements Protocol {
	public static final int PREDECESSOR_REQ_INTERVAL_IN_MS = 1000;
	public static final int FINGER_FIX_INTERVAL_IN_MS = 1000;

	private MessageSender msgSender;
	private FingerTable ft;
	private int arity;
	private long idSpace;
	private PeerEntry self;
	private String state;
	private PeerEntry predecessor;
	private PeerEntry successor;
	private PeerEntry successorList[];
	private boolean running = true;
	private long predecessorLastSeen = Long.MAX_VALUE;
	private DataStore dataStore;
	private boolean objectBufferIsSet;
	private Object objectBuffer;
	private Timer checkPredecessorTimer = new Timer(true);
	private Timer checkFingersTimer = new Timer(true);

	public Node(Address address, long idSpace, int arity) {
		this.idSpace = idSpace;
		this.arity = arity;
		long localId = IDGenerator.getId(address, idSpace);
		dataStore = new DataStore();
		objectBuffer = null;
		objectBufferIsSet = false;
		self = new PeerEntry(address, localId);
		this.ft = new FingerTable(arity, idSpace, self);
		this.successorList = new PeerEntry[3];
		predecessor = null;
		successor = self;
		state = STATE_DISCONNECTED;
		msgSender = new MessageSender(address.getPort());
		
		msgSender.registerMessageObserver(new ConcreteObserver<Message>() {
			@Override
			public void notifyObserver(Message msg) {
				handleMessage(msg);
			}	
		});
		msgSender.registerControlObserver(new ConcreteObserver<ControlEvent>() {
			@Override
			public void notifyObserver(ControlEvent e) {
				if(e instanceof ConnectionRefusedEvent)
					handleConnectionRefusedEvent((ConnectionRefusedEvent) e);
			}	
		});
		msgSender.start();

		checkPredecessorTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if(state == STATE_CONNECTED)
				{
					sendCheckPredecessor();
					sendPredecessorRequest();
				}
			}
			
		}, 1000, PREDECESSOR_REQ_INTERVAL_IN_MS);
		checkFingersTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if(state == STATE_CONNECTED) {
					fixFingers();
				}
			}
		}, 1000, FINGER_FIX_INTERVAL_IN_MS);
	}
	
	
	public String getState()
	{
		return state;
	}
	
	public PeerEntry getPredecessor()
	{
		return predecessor;
	}
	
	public PeerEntry getSuccessor()
	{
		return successor;
	}
	
	public long getId(){
		return self.getId();
	}
	
	public Address getAddress()
	{
		return self.getAddress();
	}

	public DataStore getDataStore()
	{
		return dataStore;
	}
	
	public FingerTableEntry[] getFingers() {
		return ft.getEntries();
	}
	
	public void shutdown() {
		if (running) {
			checkFingersTimer.cancel();
			checkFingersTimer.purge();
			checkPredecessorTimer.cancel();
			checkPredecessorTimer.purge();
			msgSender.stop();
			running = false;
		}
	}

	public void send(Address addr, Message msg){
		msg.setDestinationAddress(addr);
		msgSender.send(msg);
	}

	public void connect(Address addr) {
		state = STATE_CONNECTING;
		Message msg = new Message();
		msg.setKey(PROTOCOL_COMMAND, PROTOCOL_JOIN);
		msg.setKey(PROTOCOL_JOIN_ARITY, arity);
		msg.setKey(PROTOCOL_JOIN_IDENTIFIERSPACE, idSpace);
		msg.setKey(PROTOCOL_JOIN_ID, self.getId());
		send(addr, msg);		
	}

	private void sendCheckPredecessor() {
		long now = System.currentTimeMillis();
		if (predecessor != null) {
			Message msg = new Message();
			msg.setKey(PROTOCOL_COMMAND, PROTOCOL_CHECK_PREDECESSOR);
			send(predecessor.getAddress(), msg);
			if(now - predecessorLastSeen > PREDECESSOR_REQ_INTERVAL_IN_MS *4) {
				System.err.println(self + ": Predecessor (" + predecessor.getId() + ") timed out");
				predecessor = null;
			} 
		} else {
			System.out.println(self.getId() + ": Not pinging! pred = NULL");
		}
	}

	private void updatePredecessor(PeerEntry predecessor) {
		Message msg;
		predecessorLastSeen = System.currentTimeMillis();
		if (this.predecessor == null) {
			System.out.println(self.getId() + ": Changing predecessor from: NULL to: " + predecessor.getId());
		} else {
			System.out.println(self.getId() + ": changing predecessor from: " + this.predecessor.getId() +" to: " + predecessor.getId());
		}
		this.predecessor = predecessor;

		for (Map.Entry<Long, Object> e : dataStore.getAllEntriesNotBetween(predecessor.getId(), self.getId()).entrySet()) {
			System.out.println("Assigning entry: " + e.getKey() + " to new predecessor");
			msg = new Message();
			msg.setKey(PROTOCOL_COMMAND, PROTOCOL_DATA_RESPONSIBILITY);
			msg.setKey(PROTOCOL_DATA_KEY, e.getKey());
			msg.setKey(PROTOCOL_DATA_OBJECT, e.getValue());
			send(predecessor.getAddress(), msg);
		}
	}

	public void updateSuccessor(PeerEntry successor)
	{
		if (this.successor == null) {
			System.out.println(self.getId() + ": changing successor from: NULL to: " + successor.getId());
		}
		else {
			System.out.println(self.getId() + ": changing successor from: " + this.successor.getId() + " to: " + successor.getId());
		}
		this.successor = successor;
		sendSuccessorInform();
		successorList[0] = successorList[1] = successorList[2] = null;
	}

	private void sendPredecessorRequest() {
		if (!successor.equals(self)) {
			Message msg = new Message();
			msg.setKey(PROTOCOL_COMMAND, PROTOCOL_PREDECESSOR_REQUEST);
			send(successor.getAddress(), msg);
		}
	}

	private void sendSuccessorInform() {
		if (!self.equals(successor)) {
			Message msg = new Message();
			msg.setKey(PROTOCOL_COMMAND, PROTOCOL_SUCCESSORINFORM);
			msg.setKey(PROTOCOL_SENDER_ID, self.getId());
			System.out.println(self.getId()+": Sent successor inform to successor: " + successor.getId());
			send(successor.getAddress(), msg);
		}
	}

	public void handleConnectionRefusedEvent(ConnectionRefusedEvent e) {
		System.out.println(self.getId()+": Received ConnectionRefusedEvent when trying to connect to: " + e.getSource());
		if (e.getSource().equals(successor.getAddress())) {
			System.out.println(self.getId()+": My successor has disconnected!");
			if (successorList[0] != null && !successor.equals(successorList[0]) && !successorList[0].equals(self)) {
				System.out.println("Switching to next on list: "+ successorList[0].getId());
				updateSuccessor(successorList[0]);
			} else {
				System.out.println(self.getId()+": DISCONNECTED");
				state = STATE_DISCONNECTED;
				successor = self;
			}
			successorList[0] = null;
			successorList[1] = null;
			successorList[2] = null;
		}
		if (predecessor != null && e.getSource().equals(predecessor.getAddress())) {
			System.out.println("(" + self.getId() + ") setting predecessor to null");
			predecessor = null;
		}
		ft.repairFingerTable(successor, new PeerEntry(e.getSource(),IDGenerator.getId(e.getSource(), idSpace)));
	}

	public void handleMessage(Message msg) {
		Address src = msg.getSourceAddress();
		if (!msg.has(PROTOCOL_COMMAND)) {
			handleUnknownMessage(msg);
			return;
		}
		String command = (String) msg.getKey(PROTOCOL_COMMAND);

		if (command.equals(PROTOCOL_JOIN)) {
			handleJoin(msg);
		} else if(command.equals(PROTOCOL_JOIN_DENIED)){
			handleJoinDenied(msg);
		} else if(command.equals(PROTOCOL_SUCCESSORINFORM)){
			handleSuccessorInform(msg);
		} else if(command.equals(PROTOCOL_PREDECESSOR_REQUEST)){
			handlePredecessorRequest(src);
		} else if(command.equals(PROTOCOL_PREDECESSOR_RESPONSE)){
			handlePredecessorResponse(msg);
		} else if(command.equals(PROTOCOL_CHECK_PREDECESSOR)) {
			handleCheckPredecessor(msg);
		} else if(command.equals(PROTOCOL_CHECK_PREDECESSOR_RESPONSE)) {
			handleCheckPredecessorResponse();
		} else if(command.equals(PROTOCOL_FIND_SUCCESSOR)){
			handleFindSuccessor(msg);
		} else if(command.equals(PROTOCOL_FIND_SUCCESSOR_RESPONSE)){
			handleFindSuccessorResponse(msg);
		} else if(command.equals(PROTOCOL_DATA_RESPONSIBILITY)){
			handleDataResponsibility(msg);
		} else {
			handleUnknownMessage(msg);
		}
	}

	public void handleCheckPredecessor(Message msg) {
		Message response = new Message();
		response.setKey(PROTOCOL_COMMAND, PROTOCOL_CHECK_PREDECESSOR_RESPONSE);
		send(msg.getSourceAddress(), response);
	}

	public void handleCheckPredecessorResponse() {
		predecessorLastSeen = System.currentTimeMillis();
	}

	/**
     * Handle a received Join message.
     * A Join message is received when the sending node is connecting (requesting to connect) to the receiving
     * node. If the identifier space and the arity is of the same size, and the id of the connecting
     * node is supplied, the node is accepted.
     * @param msg The message containing the request with all parameters (wrapped JSONObject).
     * @return void
     */
	private void handleJoin(Message msg){
		if (state == STATE_CONNECTING) {
			System.out.println(self.getId()+": Received join request although im connecting myself. dropped.");
			return;
		}
		Address src = msg.getSourceAddress();
		Message resp = new Message();
		System.out.println(self.getId()+": Received join request ");
		if (msg.has(PROTOCOL_JOIN_ID) && msg.has(PROTOCOL_JOIN_ARITY)
				&& msg.has(PROTOCOL_JOIN_IDENTIFIERSPACE)) {
			if ((msg.getInt(PROTOCOL_JOIN_ARITY) == arity)
					&& (msg.getLong(PROTOCOL_JOIN_IDENTIFIERSPACE) == idSpace)) {
				if (state.equals(STATE_DISCONNECTED)) {
					state = STATE_CONNECTED;
					updateSuccessor(new PeerEntry(src,msg.getLong(PROTOCOL_JOIN_ID)));
					resp.setKey(PROTOCOL_COMMAND, PROTOCOL_FIND_SUCCESSOR_RESPONSE);
					resp.setKey(PROTOCOL_FIND_SUCCESSOR_COMMAND, PROTOCOL_FIND_SUCCESSOR_COMMAND_FINGERTABLE);
					resp.setKey(PROTOCOL_FIND_SUCCESSOR_KEY, msg.getLong(PROTOCOL_JOIN_ID));
					resp.setKey(PROTOCOL_FIND_SUCCESSOR_RESPONSE_ADDR, self.getAddress().toString());
					resp.setKey(PROTOCOL_FIND_SUCCESSOR_RESPONSE_ID, self.getId());
					send(src, resp);
					return;
				}
				findSuccessorForNode(new PeerEntry(src, msg.getLong(PROTOCOL_JOIN_ID)));
				return;
			}
		}
		System.out.println("(" + self.getId() + ") Denying join!");
		resp.setKey(PROTOCOL_COMMAND, PROTOCOL_DENIED);
		send(src,resp);
	}
	
	private void handleJoinDenied(Message msg){
		if (state == STATE_CONNECTING) {
			System.out.println(self.getId()+": My connection was DENIED by (" + msg.getLong(PROTOCOL_JOIN_ID));
			state = STATE_DISCONNECTED;
		} else {
			System.out.println(self.getId()+": Got join denied even though im not connecting!");
		}
	}
	
	/**
     * Handle a received SuccessorInform message.
     * A Successor inform is received when the sending node informs the receiving node that it is
     * its current predecessor. The receiving node compares its current predecessor with the sender
     * and makes a decision whether to change predecessor or not (done in silence, no information is sent out
     * regarding a possible change of predecessor).
     * @param msg The message containing the senders id.
     * @return void
     */
	private void handleSuccessorInform(Message msg) {
		System.out.println(self.getId() + " received successor inform!");
		long sender = msg.getLong(PROTOCOL_SENDER_ID);
		if (predecessor == null) {
			updatePredecessor(new PeerEntry(msg.getSourceAddress(), sender));
			return;
		}
		long predecessorId = predecessor.getId();
		if (sender != predecessorId) {
			if (isBetween(sender, predecessorId, self.getId())) {
				updatePredecessor(new PeerEntry(msg.getSourceAddress(), sender));
			}
		}
	}
	
	/**
     * Handle a received PredecessorRequest message.
     * A PredecessorRequest is sent periodically by every node to its successor, to verify that the sending node
     * still is the receiving nodes predecessor. The receiving node simply responds by informing
     * who is its current predecessor. A change of one nodes predecessor is thus made without informing
     * the former predecessor, and the PredecessorRequest messages is the way of detecting it.
     * @param src The address of the sending node
     * @return void
     */
	private void handlePredecessorRequest(Address src){
		if (state.equals(STATE_DISCONNECTED))
			return;
		
		Message response = new Message();
		response.setKey(PROTOCOL_COMMAND, PROTOCOL_PREDECESSOR_RESPONSE);
		if (predecessor != null) {
			response.setKey(PROTOCOL_PREDECESSOR_ID, predecessor.getId());
			response.setKey(PROTOCOL_PREDECESSOR_ADDRESS, predecessor.getAddress());
		} else {
			response.setKey(PROTOCOL_PREDECESSOR_ID, -1L);
		}
		response.setKey(PROTOCOL_SUCCESSORLIST_1_ADDR,successor.getAddress().toString());
		response.setKey(PROTOCOL_SUCCESSORLIST_1_ID,successor.getId());

		if (successorList[0] != null)
		{
			response.setKey(PROTOCOL_SUCCESSORLIST_2_ADDR, successorList[0].getAddress().toString());
			response.setKey(PROTOCOL_SUCCESSORLIST_2_ID, successorList[0].getId());

		}
		if (successorList[1] != null)
		{
			response.setKey(PROTOCOL_SUCCESSORLIST_3_ADDR, successorList[1].getAddress().toString());
			response.setKey(PROTOCOL_SUCCESSORLIST_3_ID, successorList[1].getId());

		}
		send(src,response);
	}
	
	/**
     * Handle a received PredecessorResponse message.
     * A PredecessorResponse is received as a response to a PredecessorRequest message
     * and contains information of the sending nodes current predecessor.
     * The receiving node is interested in knowing whether or not it is the sending nodes predecessor.
     * @param msg The message containing the senders predecessor.
     * @return void
     */
	private void handlePredecessorResponse(Message msg){
		if (state.equals(STATE_DISCONNECTED))
			return;
		long pid = msg.getLong(PROTOCOL_PREDECESSOR_ID);
		if (pid == self.getId()) {
			if(msg.has(PROTOCOL_SUCCESSORLIST_1_ID)) {
				successorList[0] = new PeerEntry(new Address(msg.getString(PROTOCOL_SUCCESSORLIST_1_ADDR)), msg.getLong(PROTOCOL_SUCCESSORLIST_1_ID));
			} if(msg.has(PROTOCOL_SUCCESSORLIST_2_ID)) {
				successorList[1] = new PeerEntry(new Address(msg.getString(PROTOCOL_SUCCESSORLIST_2_ADDR)), msg.getLong(PROTOCOL_SUCCESSORLIST_2_ID));
			} if(msg.has(PROTOCOL_SUCCESSORLIST_3_ID)) {
				successorList[2] = new PeerEntry(new Address(msg.getString(PROTOCOL_SUCCESSORLIST_3_ADDR)), msg.getLong(PROTOCOL_SUCCESSORLIST_3_ID));
			}
			return;
		} else if (pid == -1) {
			sendSuccessorInform();
			return;
		} else {
			Address addr = new Address(msg.getString(PROTOCOL_PREDECESSOR_ADDRESS));
			updateSuccessor(new PeerEntry(addr, pid));
		}
	}

	private void handleFindSuccessor(Message msg){
		Message resp;
		if (msg.getSourceAddress().equals(self.getAddress())) {
			return;
		} else if(successor == self) {
			return;
		}

		if (msg.getString(PROTOCOL_FIND_SUCCESSOR_COMMAND).equals(PROTOCOL_FIND_SUCCESSOR_COMMAND_FINGERTABLE)
				|| msg.getString(PROTOCOL_FIND_SUCCESSOR_COMMAND).equals(PROTOCOL_FIND_SUCCESSOR_COMMAND_LOOKUP))
		{
			long key = msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY);
			if (isBetween(msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY),self.getId(), successor.getId())) //Our successor owns the key, respond with that
			{
				resp = new Message();
				resp.setKey(PROTOCOL_COMMAND, PROTOCOL_FIND_SUCCESSOR_RESPONSE);
				resp.setKey(PROTOCOL_FIND_SUCCESSOR_KEY, key);
				if (msg.getString(PROTOCOL_FIND_SUCCESSOR_COMMAND).equals(PROTOCOL_FIND_SUCCESSOR_COMMAND_FINGERTABLE)) {
					resp.setKey(PROTOCOL_FIND_SUCCESSOR_COMMAND, PROTOCOL_FIND_SUCCESSOR_COMMAND_FINGERTABLE);
				} else {
					resp.setKey(PROTOCOL_FIND_SUCCESSOR_COMMAND, PROTOCOL_FIND_SUCCESSOR_COMMAND_LOOKUP);
				}
				resp.setKey(PROTOCOL_FIND_SUCCESSOR_RESPONSE_ADDR, successor.getAddress().toString());
				resp.setKey(PROTOCOL_FIND_SUCCESSOR_RESPONSE_ID, successor.getId());
				send(new Address(msg.getString(PROTOCOL_FIND_SUCCESSOR_SENDER_ADDR)), resp);
			} else if (ft.closestPrecedingNode(key) != self) {
				send(ft.closestPrecedingNode(key).getAddress(), msg);
			}
			
		} else if (isBetween(msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY), predecessor.getId(), self.getId())) //Our responsibility
		{
			System.out.println(self.getId() + ": handling a " + msg.getString(PROTOCOL_FIND_SUCCESSOR_COMMAND) + " from: " + msg.getSourceAddress().toString());
			if (msg.getString(PROTOCOL_FIND_SUCCESSOR_COMMAND).equals(PROTOCOL_FIND_SUCCESSOR_COMMAND_GET)) //Only message to reply for: Get-requests
			{
				resp = new Message();
				resp.setKey(PROTOCOL_COMMAND, PROTOCOL_FIND_SUCCESSOR_RESPONSE);
				resp.setKey(PROTOCOL_FIND_SUCCESSOR_COMMAND, PROTOCOL_FIND_SUCCESSOR_COMMAND_GET);
				resp.setKey(PROTOCOL_FIND_SUCCESSOR_KEY, msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY));

				if (dataStore.getEntry(msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY)) != null)
				{
					resp.setKey(PROTOCOL_FIND_SUCCESSOR_RESPONSE_OBJECT, dataStore.getEntry(msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY)).toString());
				} else
				{
					resp.setKey(PROTOCOL_FIND_SUCCESSOR_RESPONSE_OBJECT, PROTOCOL_NULL);
				}
				send(new Address(msg.getString(PROTOCOL_FIND_SUCCESSOR_SENDER_ADDR)), resp);
			} else
			{
				if(msg.getString(PROTOCOL_FIND_SUCCESSOR_COMMAND).equals(PROTOCOL_FIND_SUCCESSOR_COMMAND_PUT))
				{
					dataStore.addEntry(msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY), msg.getString(PROTOCOL_FIND_SUCCESSOR_PUT_OBJECT));
				} else if(msg.getString(PROTOCOL_FIND_SUCCESSOR_COMMAND).equals(PROTOCOL_FIND_SUCCESSOR_COMMAND_REMOVE))
				{
					dataStore.removeEntry(msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY));
				}
			}
		} else
		{
			if(isBetween(msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY), self.getId(), successor.getId())) //Successor's responsibility
			{
				send(successor.getAddress(), msg);
			}
			else
			{
				send(ft.closestPrecedingNode(msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY)).getAddress(), msg);
			}
		}
	}

	private void handleFindSuccessorResponse(Message msg){
		if (msg.getString(PROTOCOL_FIND_SUCCESSOR_COMMAND).equals(PROTOCOL_FIND_SUCCESSOR_COMMAND_FINGERTABLE)) {
			if (state == STATE_CONNECTING) {
				if (msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY) == self.getId()) {
					System.out.println(self.getId() + ": Connection Accepted!");
					updateSuccessor(new PeerEntry(new Address(msg.getString(PROTOCOL_FIND_SUCCESSOR_RESPONSE_ADDR)),
							msg.getInt(PROTOCOL_FIND_SUCCESSOR_RESPONSE_ID)));
					state = STATE_CONNECTED;
				}
			}
			PeerEntry pe = new PeerEntry(new Address(msg.getString(PROTOCOL_FIND_SUCCESSOR_RESPONSE_ADDR)),
					msg.getLong(PROTOCOL_FIND_SUCCESSOR_RESPONSE_ID));
			ft.setFingerEntry(msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY), pe);
		} else if (msg.getString(PROTOCOL_FIND_SUCCESSOR_COMMAND).equals(PROTOCOL_FIND_SUCCESSOR_COMMAND_LOOKUP)) {
			System.out.println("Lookup result for key: " + msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY)
					+ ">> node: " + msg.getLong(PROTOCOL_FIND_SUCCESSOR_RESPONSE_ID));
		} else if (msg.getString(PROTOCOL_FIND_SUCCESSOR_COMMAND).equals(PROTOCOL_FIND_SUCCESSOR_COMMAND_GET)) {
			if (!msg.getString(PROTOCOL_FIND_SUCCESSOR_RESPONSE_OBJECT).equals(PROTOCOL_NULL))
			{
				objectBufferIsSet = true;
				objectBuffer = msg.getString(PROTOCOL_FIND_SUCCESSOR_RESPONSE_OBJECT);
			}
		}
	}

	/**
     * Handle a received Dataresponsibility message
     * @param msg The address of the sending node.
     * @return void
     */
	private void handleDataResponsibility(Message msg){
		if(msg.has(PROTOCOL_DATA_KEY) && msg.has(PROTOCOL_DATA_OBJECT)) {
			System.out.println(self.getId() + ": Adding data with key: " + msg.getLong(PROTOCOL_DATA_KEY) + " to my storage");
			dataStore.addEntry(msg.getLong(PROTOCOL_DATA_KEY), msg.getString(PROTOCOL_DATA_OBJECT));
		}
	}
	
	/**
     * Handle a message with unknown syntax
     * Current version of the protocol just drops it and writes to stdout about it.
     * @param msg The message containing the senders id.
     * @return void
     */
	private void handleUnknownMessage(Message msg){
		System.out.println(self.getId()+": Received unknown message: " +msg.toString());
		return;
	}

	public void fixFingers()
	{
		for (FingerTableEntry e : ft.getEntries()) {
			if (e.getPeerEntry() != null) {
				findSuccessor(e.getKey());
			}	
		}
	}

	public void findSuccessor(long key)
	{
		Message msg;
		
		if (isBetween(key,self.getId(), successor.getId())) {
			ft.setFingerEntry(key, successor);
		} else if (key == self.getId()) {
			ft.setFingerEntry(key, self);
		} else if (ft.closestPrecedingNode(key) != null) {
			msg = new Message();
			msg.setKey(PROTOCOL_COMMAND, PROTOCOL_FIND_SUCCESSOR);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_KEY, key);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_COMMAND, PROTOCOL_FIND_SUCCESSOR_COMMAND_FINGERTABLE);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_SENDER_ADDR, self.getAddress().toString());
			send(ft.closestPrecedingNode(key).getAddress(), msg);
		}
	}
	
	private void findSuccessorForNode(PeerEntry source){
		if (isBetween(source.getId(),self.getId(), successor.getId())) {
			Message resp = new Message();
			resp.setKey(PROTOCOL_COMMAND, PROTOCOL_FIND_SUCCESSOR_RESPONSE);
			resp.setKey(PROTOCOL_FIND_SUCCESSOR_KEY, source.getId());
			resp.setKey(PROTOCOL_FIND_SUCCESSOR_COMMAND, PROTOCOL_FIND_SUCCESSOR_COMMAND_FINGERTABLE);
			resp.setKey(PROTOCOL_FIND_SUCCESSOR_RESPONSE_ADDR, successor.getAddress().toString());
			resp.setKey(PROTOCOL_FIND_SUCCESSOR_RESPONSE_ID, successor.getId());
			send(source.getAddress(), resp);
			updateSuccessor(source);
		}
		else if (ft.closestPrecedingNode(source.getId()) != null) {
			Message msg = new Message();
			msg.setKey(PROTOCOL_COMMAND, PROTOCOL_FIND_SUCCESSOR);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_COMMAND, PROTOCOL_FIND_SUCCESSOR_COMMAND_FINGERTABLE);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_KEY, source.getId());
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_SENDER_ADDR, source.getAddress().toString());
			PeerEntry entry = ft.closestPrecedingNode(source.getId());
			if (entry != null) {
				send(entry.getAddress(), msg);
			}
		}
	}

	public Object getObject(long key)
	{
		Message msg;
		if (state != STATE_CONNECTED || isBetween(key, predecessor.getId(), self.getId())) {
			System.out.println(self.getId() + ": getting data from own storage");
			return dataStore.getEntry(key);
		} else {
			objectBuffer = null;
			objectBufferIsSet = false;
			msg = new Message();
			msg.setKey(PROTOCOL_COMMAND, PROTOCOL_FIND_SUCCESSOR);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_KEY, key);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_COMMAND, PROTOCOL_FIND_SUCCESSOR_COMMAND_GET);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_SENDER_ADDR, self.getAddress().toString());
			if (isBetween(key, self.getId(), successor.getId())) {
				System.out.println(self.getId() + ": passing getObject-request to successor");
				send(successor.getAddress(), msg);
				for (int i=0;i<2;i++) {
					if (objectBufferIsSet == true) {
						Object temp = objectBuffer;
						objectBuffer = null;
						objectBufferIsSet = false;
						return temp;
					} try {
						Thread.sleep(1000);
					} catch(Exception e)
					{
						System.out.println("Couldn't sleep thread");
					}
				}
				return null;
			} else {
				System.out.println(self.getId() + ": sending getObject-request a long");
				send(ft.closestPrecedingNode(key).getAddress(), msg);
				for (int i=0;i<5;i++) {
					if (objectBufferIsSet == true) {
						Object temp = objectBuffer;
						objectBuffer = null;
						objectBufferIsSet = false;
						return temp;
					}
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						System.out.println("Couldn't sleep thread");
					}
				}
				return null;
			}
		}
	}

	public void putObject(long key, Object object) {
		Message msg;
		if (state != STATE_CONNECTED || isBetween(key, predecessor.getId(), self.getId())) {
			System.out.println(self.getId() + ": adding data to own storage");
			dataStore.addEntry(key, object);
		} else {
			msg = new Message();
			msg.setKey(PROTOCOL_COMMAND, PROTOCOL_FIND_SUCCESSOR);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_KEY, key);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_COMMAND, PROTOCOL_FIND_SUCCESSOR_COMMAND_PUT);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_PUT_OBJECT, object.toString());
			if (isBetween(key, self.getId(), successor.getId())) {
				System.out.println(self.getId() + ": passing putObject-request to successor");
				send(successor.getAddress(), msg);
			} else {
				System.out.println(self.getId() + ": sending putObject-request a long");
				send(ft.closestPrecedingNode(key).getAddress(), msg);
			}
		}
	}

	public void removeObject(long key) {
		Message msg;
		if (state != STATE_CONNECTED || isBetween(key, predecessor.getId(), self.getId())) {
			System.out.println(self.getId() + ": removing data from own storage");
			dataStore.removeEntry(key);
		} else {
			msg = new Message();
			msg.setKey(PROTOCOL_COMMAND, PROTOCOL_FIND_SUCCESSOR);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_KEY, key);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_COMMAND, PROTOCOL_FIND_SUCCESSOR_COMMAND_REMOVE);
			if (isBetween(key, self.getId(), successor.getId())) {
				System.out.println(self.getId() + ": passing removeObject-request to successor");
				send(successor.getAddress(), msg);
			} else {
				System.out.println(self.getId() + ": sending removeObject-request a long");
				send(ft.closestPrecedingNode(key).getAddress(), msg);
			}
		}
	}

	public static boolean isBetween(long l_1, long l_2, long l_3) {
		if(l_1 == l_3)
			return true;
		long shifted_l2 = l_2 - l_1;
		long shifted_l3 = l_3 - l_1;
		if((shifted_l2 < 0) && (shifted_l3 < 0))
			return (shifted_l2 > shifted_l3);
		else if(shifted_l3 < 0)
			return false;
		else if(shifted_l2 < 0)
			return true;
		else
			return (shifted_l2 > shifted_l3);
	}
	
	public long getIdSpace() {
		return idSpace;
	}
}

