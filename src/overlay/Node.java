package overlay;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import overlay.datastorage.Datastore;


import network.Address;
import network.ConcreteObserver;
import network.MessageSender;
import network.events.ConnectionRefusedEvent;
import network.events.ControlEvent;
import network.events.DisconnectEvent;
import network.events.Message;

public class Node implements Protocol {

	private MessageSender msgSender;
	private FingerTable ft;
	private int arity;
	private long idSpace;
	private PeerEntry self;
	private String state;

	private PeerEntry predecessor;
	private PeerEntry successor;
	private PeerEntry successorlist[];
	private boolean running = true;
	private long predecessorLastSeen = Long.MAX_VALUE;
	
	private Datastore datastore;
	private boolean objectBufferIsSet;
	private Object objectBuffer; /* Used to store retrieved objects during lookups (as lookups are done unblocking) */
	
	public static final int PRED_REQ_INTERVAL = 1000;
	public static final int FINGER_FIX_INTERVAL = 1000;

	private Timer checkPredecessorTimer = new Timer(true);
	private Timer checkFingersTimer = new Timer(true);
	
	public Node(Address addr, long idSpace, int arity) {
		this.idSpace = idSpace;
		this.arity = arity;
		long localId = IDGenerator.getId(addr, idSpace);
		datastore = new Datastore();
		objectBuffer = null;
		objectBufferIsSet = false;
		self = new PeerEntry(addr, localId);
		this.ft = new FingerTable(arity, idSpace, self);
		this.successorlist = new PeerEntry[3];
		predecessor = null;
		successor = self;
		state = STATE_DISCONNECTED;
		msgSender = new MessageSender(addr.getPort());
		msgSender.registerMessageObserver(new ConcreteObserver<Message>() {
			@Override
			public void notifyObserver(Message msg) {
				handleMessage(msg);
			}	
		});
		msgSender.registerControlObserver(new ConcreteObserver<ControlEvent>() {
			@Override
			public void notifyObserver(ControlEvent e) {
				if(e instanceof DisconnectEvent)
					handleDisconnectEvent((DisconnectEvent) e);
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
					System.out.println(self.getId() + " checking predecessor. ");
					sendCheckPredecessor();
					sendPredRequest();
				}
			}
			
		}, 1000, PRED_REQ_INTERVAL);
		
		checkFingersTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if(state == STATE_CONNECTED) {
					System.out.println(self.getId() + " fixing fingers ");
					fixFingers();
				}
			}
		}, 1000, FINGER_FIX_INTERVAL); 
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
	
	public Datastore getDatastore()
	{
		return datastore;
	}
	
	public void shutdown()
	{
		if(running) {
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
	//	System.out.println("Sending msg: " + msg.toString() + ", to addr: " + addr);
		msgSender.send(msg);
	}
	
	public void connect(Address addr)
	{
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
		if(predecessor != null) {
			//System.out.println(self.getId() + "Pinging to addr: "+ predecessor.getAddress());
			Message msg = new Message();
			msg.setKey(PROTOCOL_COMMAND, PROTOCOL_CHECK_PREDECESSOR);
			send(predecessor.getAddress(), msg);
			if(now - predecessorLastSeen > PRED_REQ_INTERVAL*4) {
				System.err.println(self + ": Predecessor (" + predecessor.getId() + ") timed out");
				predecessor = null;
			} 
		} else
		{
			System.out.println(self.getId() + ": Not pinging! pred = NULL");
		}
	}
	
	private void updatePredecessor(PeerEntry predecessor) {
		Message msg;
		predecessorLastSeen = System.currentTimeMillis();
		if(this.predecessor == null)
		{
			System.out.println(self.getId() + ": Changing predecessor from: NULL to: " + predecessor.getId());
		}
		else{
			System.out.println(self.getId() + ": changing predecessor from: " + this.predecessor.getId() +" to: " + predecessor.getId());
		}
		this.predecessor = predecessor;
		//Send over any stored data which key's is not in between the predecessor and self in the ring to the predecessor.
		for(Map.Entry<Long,Object> e : datastore.getAllEntriesNotBetween(predecessor.getId(), self.getId()).entrySet())
		{
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
		if(this.successor == null)
		{
			System.out.println(self.getId() + ": changing successor from: NULL to: " + successor.getId());
		}
		else{
			System.out.println(self.getId() + ": changing successor from: " + this.successor.getId() +" to: " + successor.getId());

		}
		this.successor = successor;
		sendSuccessorInform();
		successorlist[0] = successorlist[1] = successorlist[2] = null;
	}
	
	private void sendPredRequest() {
		if(!successor.equals(self)) {
			Message msg = new Message();
			msg.setKey(PROTOCOL_COMMAND, PROTOCOL_PREDECESSOR_REQUEST);
			send(successor.getAddress(), msg);
		}
	}
	
	private void sendSuccessorInform() {
		if(!self.equals(successor)) {
			Message msg = new Message();
			msg.setKey(PROTOCOL_COMMAND, PROTOCOL_SUCCESSORINFORM);
			msg.setKey(PROTOCOL_SENDER_ID, self.getId());
			System.out.println(self.getId()+": Sent successor inform to successor: " + successor.getId());
			send(successor.getAddress(), msg);
		}
	}
	
	public void handleDisconnectEvent(DisconnectEvent e) {
		System.out.println(self.getId()+": Received DisconnectEvent from some host!");
	}
	
	public void handleConnectionRefusedEvent(ConnectionRefusedEvent e) {
		System.out.println(self.getId()+": Received ConnectionRefusedEvent when trying to connect to: " + e.getSource());
		if(e.getSource().equals(successor.getAddress()))
		{
			System.out.println(self.getId()+": My successor has disconnected!");
			if(successorlist[0] != null && !successor.equals(successorlist[0]) && !successorlist[0].equals(self))
			{
				System.out.println("Switching to next on list: "+successorlist[0].getId());
				updateSuccessor(successorlist[0]);
			}
			else
			{
				System.out.println(self.getId()+": DISCONNECTED");
				state = STATE_DISCONNECTED;
				successor = self;
			}
			successorlist[0] = null;
			successorlist[1] = null;
			successorlist[2] = null;
		} if(predecessor != null)
		{
			if(e.getSource().equals(predecessor.getAddress()))
			{
				System.out.println("("+self.getId()+") setting predecessor to null");
				predecessor = null;				
			}
		}
		ft.repairFingerTable(successor, new PeerEntry(e.getSource(),IDGenerator.getId(e.getSource(), idSpace)));
	}
	
	public void handleMessage(Message msg) {
		Address src = msg.getSourceAddress();
		if(!msg.has(PROTOCOL_COMMAND))
		{
			//Not following protocol, disregard it!
			handleUnknownMessage(msg);
			return;
		}
		String command = (String) msg.getKey(PROTOCOL_COMMAND);
		//System.out.println(self.getId()+": Received command " + command + " from: " + src);

		if(command.equals(PROTOCOL_JOIN))
		{
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
			handleCheckPredResponse(msg);
		} else if(command.equals(PROTOCOL_FIND_SUCCESSOR)){
			handleFindSuccessor(msg);
		} else if(command.equals(PROTOCOL_FIND_SUCCESSOR_RESPONSE)){
			handleFindSuccessorResponse(msg);
		} else if(command.equals(PROTOCOL_DATA_RESPONSIBILITY)){
			handleDataresponsibility(msg);
		}else
		{
			handleUnknownMessage(msg);
		}
	}

	/** Respond on predecessor pings */
	public void handleCheckPredecessor(Message msg) {
		//System.out.println(self.getId() + " responding to ping");
		Message response = new Message();
		response.setKey(PROTOCOL_COMMAND, PROTOCOL_CHECK_PREDECESSOR_RESPONSE);
		send(msg.getSourceAddress(), response);
	}
	
	/** Update predecessorLastSeen when a ping response is received.*/
	public void handleCheckPredResponse(Message msg) {
		predecessorLastSeen = System.currentTimeMillis();
	}
	
	/* 
	 * Protocol specific methods handling implemented protocol messages.
	 * Methods are passed either the sender's address or if necessary, also the message received.
	 * With the current state and the address/message as basis, the method acts accordingly.
	 */
	
	/**
     * Handle a received Join message.
     * A Join message is received when the sending node is connecting (requesting to connect) to the receiving
     * node. If the identifier space and the arity is of the same size, and the id of the connecting
     * node is supplied, the node is accepted.
     * @param msg The message containing the request with all parameters (wrapped JSONObject).
     * @return void
     */
	private void handleJoin(Message msg){
		if(state == STATE_CONNECTING)
		{
			System.out.println(self.getId()+": Received join request although im connecting myself. dropped.");
			return;
		}
		Address src = msg.getSourceAddress();
		Message resp = new Message();
		System.out.println(self.getId()+": Received join request ");
		if(msg.has(PROTOCOL_JOIN_ID) && msg.has(PROTOCOL_JOIN_ARITY)
				&& msg.has(PROTOCOL_JOIN_IDENTIFIERSPACE))
		{
			if((msg.getInt(PROTOCOL_JOIN_ARITY) == arity)
					&& (msg.getLong(PROTOCOL_JOIN_IDENTIFIERSPACE) == idSpace))
			{
				//Accept the join!
				if(state.equals(STATE_DISCONNECTED))
				{
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
		if(state==STATE_CONNECTING)
		{
			System.out.println(self.getId()+": My connection was DENIED by (" + msg.getLong(PROTOCOL_JOIN_ID));
			state = STATE_DISCONNECTED;
		}
		else
		{
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
	private void handleSuccessorInform(Message msg){
		System.out.println(self.getId() + " received successor inform!");
		long sender = msg.getLong(PROTOCOL_SENDER_ID);
		if(predecessor == null) {
			updatePredecessor(new PeerEntry(msg.getSourceAddress(), sender));
			return;
		}
		long predid = predecessor.getId();
		if(sender != predid) {
			if(isBetween(sender, predid, self.getId())) {
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
		if(state.equals(STATE_DISCONNECTED))
			return;
		
		Message response = new Message();
		response.setKey(PROTOCOL_COMMAND, PROTOCOL_PREDECESSOR_RESPONSE);
		if(predecessor != null) {
			response.setKey(PROTOCOL_PREDECESSOR_ID, predecessor.getId());
			response.setKey(PROTOCOL_PREDECESSOR_ADDRESS, predecessor.getAddress());
		} else {
			response.setKey(PROTOCOL_PREDECESSOR_ID, -1L);
		}
		response.setKey(PROTOCOL_SUCCESSORLIST_1_ADDR,successor.getAddress().toString());
		response.setKey(PROTOCOL_SUCCESSORLIST_1_ID,successor.getId());

		if(successorlist[0] != null)
		{
			response.setKey(PROTOCOL_SUCCESSORLIST_2_ADDR,successorlist[0].getAddress().toString());
			response.setKey(PROTOCOL_SUCCESSORLIST_2_ID,successorlist[0].getId());

		}
		if(successorlist[1] != null)
		{
			response.setKey(PROTOCOL_SUCCESSORLIST_3_ADDR,successorlist[1].getAddress().toString());
			response.setKey(PROTOCOL_SUCCESSORLIST_3_ID,successorlist[1].getId());

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
		if(state.equals(STATE_DISCONNECTED))
			return;
		long pid = msg.getLong(PROTOCOL_PREDECESSOR_ID);
		if (pid == self.getId())
		{
			
			if(msg.has(PROTOCOL_SUCCESSORLIST_1_ID))
			{
				successorlist[0] = new PeerEntry(new Address(msg.getString(PROTOCOL_SUCCESSORLIST_1_ADDR)), msg.getLong(PROTOCOL_SUCCESSORLIST_1_ID));
			} if(msg.has(PROTOCOL_SUCCESSORLIST_2_ID))
			{
				successorlist[1] = new PeerEntry(new Address(msg.getString(PROTOCOL_SUCCESSORLIST_2_ADDR)), msg.getLong(PROTOCOL_SUCCESSORLIST_2_ID));

			} if(msg.has(PROTOCOL_SUCCESSORLIST_3_ID))
			{
				successorlist[2] = new PeerEntry(new Address(msg.getString(PROTOCOL_SUCCESSORLIST_3_ADDR)), msg.getLong(PROTOCOL_SUCCESSORLIST_3_ID));
			}
			return;
		} else if (pid == -1)
		{
			sendSuccessorInform();
			return;
		}else { //(pid != self.getId())
			Address addr = new Address(msg.getString(PROTOCOL_PREDECESSOR_ADDRESS));
			updateSuccessor(new PeerEntry(addr, pid));
		}
	}
	
	/**
     * Handle a received FindSuccessor
     * @param src The address of the sending node.
     * @return void
     */
	private void handleFindSuccessor(Message msg){
		Message resp;
		if(msg.getSourceAddress().equals(self.getAddress()))
		{
			//System.out.println(self.getId()+": Received findsuccessor from self");
			return;
		} else if(successor == self) //Received findSuccessor when disconnected (Byzantine msg)
		{
			return;
		}
		// If the request is a lookup, or fingertable-update:
		if(msg.getString(PROTOCOL_FIND_SUCCESSOR_COMMAND).equals(PROTOCOL_FIND_SUCCESSOR_COMMAND_FINGERTABLE)
				|| msg.getString(PROTOCOL_FIND_SUCCESSOR_COMMAND).equals(PROTOCOL_FIND_SUCCESSOR_COMMAND_LOOKUP))
		{
			long key = msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY);
			if(isBetween(msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY),self.getId(), successor.getId())) //Our successor owns the key, respond with that
			{
				resp = new Message();
				resp.setKey(PROTOCOL_COMMAND, PROTOCOL_FIND_SUCCESSOR_RESPONSE);
				resp.setKey(PROTOCOL_FIND_SUCCESSOR_KEY, key);
				if(msg.getString(PROTOCOL_FIND_SUCCESSOR_COMMAND).equals(PROTOCOL_FIND_SUCCESSOR_COMMAND_FINGERTABLE))
				{
					resp.setKey(PROTOCOL_FIND_SUCCESSOR_COMMAND, PROTOCOL_FIND_SUCCESSOR_COMMAND_FINGERTABLE);
				} else
				{
					resp.setKey(PROTOCOL_FIND_SUCCESSOR_COMMAND, PROTOCOL_FIND_SUCCESSOR_COMMAND_LOOKUP);
				}
				resp.setKey(PROTOCOL_FIND_SUCCESSOR_RESPONSE_ADDR, successor.getAddress().toString());
				resp.setKey(PROTOCOL_FIND_SUCCESSOR_RESPONSE_ID, successor.getId());
				send(new Address(msg.getString(PROTOCOL_FIND_SUCCESSOR_SENDER_ADDR)), resp);
			} else if(ft.closestPrecedingNode(key) == self) //Fingertable not built, unable to handle this request.
			{
				//System.out.println("!!! ID("+self.getId()+") Fingertable broken. Returned null when looking for key: " + key);
			} else //Send request along
			{
				//System.out.println("!!! ID("+self.getId()+") routing message to: " + ft.closestPrecedingNode(key).getId());
				send(ft.closestPrecedingNode(key).getAddress(), msg);
			}
			
		}else //The request is a get / put /delete
		{
			if(isBetween(msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY), predecessor.getId(), self.getId())) //Our responsibility
			{
				System.out.println(self.getId() + ": handling a " + msg.getString(PROTOCOL_FIND_SUCCESSOR_COMMAND) + " from: " + msg.getSourceAddress().toString());
				if(msg.getString(PROTOCOL_FIND_SUCCESSOR_COMMAND).equals(PROTOCOL_FIND_SUCCESSOR_COMMAND_GET)) //Only message to reply for: Get-requests
				{
					resp = new Message();
					resp.setKey(PROTOCOL_COMMAND, PROTOCOL_FIND_SUCCESSOR_RESPONSE);
					resp.setKey(PROTOCOL_FIND_SUCCESSOR_COMMAND, PROTOCOL_FIND_SUCCESSOR_COMMAND_GET);
					resp.setKey(PROTOCOL_FIND_SUCCESSOR_KEY, msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY));

					if(datastore.getEntry(msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY)) != null)
					{
						resp.setKey(PROTOCOL_FIND_SUCCESSOR_RESPONSE_OBJECT, datastore.getEntry(msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY)).toString());
					} else
					{
						resp.setKey(PROTOCOL_FIND_SUCCESSOR_RESPONSE_OBJECT, PROTOCOL_NULL);
					}
					send(new Address(msg.getString(PROTOCOL_FIND_SUCCESSOR_SENDER_ADDR)), resp);
				} else
				{
					if(msg.getString(PROTOCOL_FIND_SUCCESSOR_COMMAND).equals(PROTOCOL_FIND_SUCCESSOR_COMMAND_PUT))
					{
						datastore.addEntry(msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY), msg.getString(PROTOCOL_FIND_SUCCESSOR_PUT_OBJECT));
					} else if(msg.getString(PROTOCOL_FIND_SUCCESSOR_COMMAND).equals(PROTOCOL_FIND_SUCCESSOR_COMMAND_REMOVE))
					{
						datastore.removeEntry(msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY));
					}
				}
			} else //Pass request along
			{
				if(isBetween(msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY), self.getId(), successor.getId())) //Successor's responsibility
				{
					/*
					System.out.println(self.getId()  + "Passing along: " + msg.getString(PROTOCOL_FIND_SUCCESSOR_COMMAND)
							+ " from: " + msg.getSourceAddress().toString() + " with key: " + msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY));
					*/
					send(successor.getAddress(), msg);
				}
				else
				{
					/*
					 System.out.println(self.getId()  + "Passing along: " + msg.getString(PROTOCOL_FIND_SUCCESSOR_COMMAND)
					 + " from: " + msg.getSourceAddress().toString() + " with key: " + msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY));
					 */
					send(ft.closestPrecedingNode(msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY)).getAddress(), msg);
				}
			}
		}
		
	}

	/**
     * Handle a received FindSuccessorResponse
     * @param src The address of the sending node.
     * @return void
     */
	private void handleFindSuccessorResponse(Message msg){
		//System.out.println(self.getId()+": Received find successor response");
		if(msg.getString(PROTOCOL_FIND_SUCCESSOR_COMMAND).equals(PROTOCOL_FIND_SUCCESSOR_COMMAND_FINGERTABLE))
		{
			if(state == Node.STATE_CONNECTING)
			{
				if(msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY) == self.getId())
				{
					System.out.println(self.getId() + ": Connection Accepted!");
					updateSuccessor(new PeerEntry(new Address(msg.getString(PROTOCOL_FIND_SUCCESSOR_RESPONSE_ADDR)),
							msg.getInt(PROTOCOL_FIND_SUCCESSOR_RESPONSE_ID)));
					state = Node.STATE_CONNECTED;
				}
			}
			PeerEntry pe = new PeerEntry(new Address(msg.getString(PROTOCOL_FIND_SUCCESSOR_RESPONSE_ADDR)),
					msg.getLong(PROTOCOL_FIND_SUCCESSOR_RESPONSE_ID));
			ft.setFingerEntry(msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY), pe);
		} else if(msg.getString(PROTOCOL_FIND_SUCCESSOR_COMMAND).equals(PROTOCOL_FIND_SUCCESSOR_COMMAND_LOOKUP))
		{
			System.out.println("Lookup result for key: " + msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY)
					+ ">> node: " + msg.getLong(PROTOCOL_FIND_SUCCESSOR_RESPONSE_ID));
		} else if(msg.getString(PROTOCOL_FIND_SUCCESSOR_COMMAND).equals(PROTOCOL_FIND_SUCCESSOR_COMMAND_GET))
		{
			if(!msg.getString(PROTOCOL_FIND_SUCCESSOR_RESPONSE_OBJECT).equals(PROTOCOL_NULL))
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
	private void handleDataresponsibility(Message msg){
		//System.out.println(self.getId()+": Received find successor response");
		if(msg.has(PROTOCOL_DATA_KEY) && msg.has(PROTOCOL_DATA_OBJECT))
		{
			System.out.println(self.getId() + ": Adding data with key: " + msg.getLong(PROTOCOL_DATA_KEY) + " to my storage");
			datastore.addEntry(msg.getLong(PROTOCOL_DATA_KEY), msg.getString(PROTOCOL_DATA_OBJECT));
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
	
	public FingerEntry[] getFingers() {
		return ft.getEntries();
	}
	
	public void fixFingers()
	{
		for(FingerEntry e : ft.getEntries())
		{
			if(e.getPeerEntry() != null)
			{
				findSuccessor(e.getKey());
			}	
		}
	}
	
	/* Recursive ring lookup */
	public void findSuccessor(long key)
	{
		Message msg;
		
		if(isBetween(key,self.getId(), successor.getId()))
		{
			ft.setFingerEntry(key, successor);
		} else if(key == self.getId())
		{
			ft.setFingerEntry(key, self);
		} else if(ft.closestPrecedingNode(key) != null)
		{
			msg = new Message();
			msg.setKey(PROTOCOL_COMMAND, PROTOCOL_FIND_SUCCESSOR);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_KEY, key);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_COMMAND, PROTOCOL_FIND_SUCCESSOR_COMMAND_FINGERTABLE);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_SENDER_ADDR, self.getAddress().toString());
			send(ft.closestPrecedingNode(key).getAddress(), msg);
		}
	}
	
	private void findSuccessorForNode(PeerEntry source){
		if(isBetween(source.getId(),self.getId(), successor.getId()))
		{
			Message resp = new Message();
			resp.setKey(PROTOCOL_COMMAND, PROTOCOL_FIND_SUCCESSOR_RESPONSE);
			resp.setKey(PROTOCOL_FIND_SUCCESSOR_KEY, source.getId());
			resp.setKey(PROTOCOL_FIND_SUCCESSOR_COMMAND, PROTOCOL_FIND_SUCCESSOR_COMMAND_FINGERTABLE);
			resp.setKey(PROTOCOL_FIND_SUCCESSOR_RESPONSE_ADDR, successor.getAddress().toString());
			resp.setKey(PROTOCOL_FIND_SUCCESSOR_RESPONSE_ID, successor.getId());
			send(source.getAddress(), resp);
			updateSuccessor(source);
		}
		else if(ft.closestPrecedingNode(source.getId()) != null)
		{
			Message msg = new Message();
			msg.setKey(PROTOCOL_COMMAND, PROTOCOL_FIND_SUCCESSOR);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_COMMAND, PROTOCOL_FIND_SUCCESSOR_COMMAND_FINGERTABLE);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_KEY, source.getId());
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_SENDER_ADDR, source.getAddress().toString());
			PeerEntry entry = ft.closestPrecedingNode(source.getId());
			if(entry != null) {
				send(entry.getAddress(), msg);
			}
		}
	}
	
	public void lookup(long key)
	{
		if(isBetween(key,self.getId(), successor.getId()))
		{
			System.out.println("lookup(" + key + ") >> nodeId:" +successor.getId());
		}
		else if(ft.closestPrecedingNode(key) != null)
		{
			Message msg = new Message();
			msg.setKey(PROTOCOL_COMMAND, PROTOCOL_FIND_SUCCESSOR);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_KEY, key);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_COMMAND, PROTOCOL_FIND_SUCCESSOR_COMMAND_LOOKUP);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_SENDER_ADDR, self.getAddress().toString());
			send(ft.closestPrecedingNode(key).getAddress(), msg);
		}
	}
	
	public Object getObject(long key)
	{
		Message msg;
		if(state != STATE_CONNECTED || isBetween(key, predecessor.getId(), self.getId()))
		{
			System.out.println(self.getId() + ": getting data from own storage");
			return datastore.getEntry(key);
		} else
		{
			objectBuffer = null;
			objectBufferIsSet = false;
			msg = new Message();
			msg.setKey(PROTOCOL_COMMAND, PROTOCOL_FIND_SUCCESSOR);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_KEY, key);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_COMMAND, PROTOCOL_FIND_SUCCESSOR_COMMAND_GET);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_SENDER_ADDR, self.getAddress().toString());
			if(isBetween(key, self.getId(), successor.getId()))
			{
				System.out.println(self.getId() + ": passing getObject-request to successor");
				send(successor.getAddress(), msg);
				for(int i=0;i<2;i++)
				{
					if(objectBufferIsSet == true)
					{
						Object temp = objectBuffer;
						objectBuffer = null;
						objectBufferIsSet = false;
						return temp;
					}
					try
					{
						Thread.sleep(1000);
					}catch(Exception e)
					{
						System.out.println("Couldn't sleep thread");
					}
				}
				return null;
			} else
			{
				System.out.println(self.getId() + ": sending getObject-request a long");
				send(ft.closestPrecedingNode(key).getAddress(), msg);
				for(int i=0;i<5;i++)
				{
					if(objectBufferIsSet == true)
					{
						Object temp = objectBuffer;
						objectBuffer = null;
						objectBufferIsSet = false;
						return temp;
					}
					try
					{
						Thread.sleep(1000);
					}catch(Exception e)
					{
						System.out.println("Couldn't sleep thread");
					}
				}
				return null;
			}
		}
	}
	
	public void putObject(long key, Object object)
	{
		Message msg;
		if(state != STATE_CONNECTED || isBetween(key, predecessor.getId(), self.getId()))
		{
			System.out.println(self.getId() + ": adding data from own storage");
			datastore.addEntry(key, object);
		} else
		{
			msg = new Message();
			msg.setKey(PROTOCOL_COMMAND, PROTOCOL_FIND_SUCCESSOR);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_KEY, key);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_COMMAND, PROTOCOL_FIND_SUCCESSOR_COMMAND_PUT);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_PUT_OBJECT, object.toString());
			if(isBetween(key, self.getId(), successor.getId()))
			{
				System.out.println(self.getId() + ": passing putObject-request to successor");
				send(successor.getAddress(), msg);
			} else
			{
				System.out.println(self.getId() + ": sending putObject-request a long");
				send(ft.closestPrecedingNode(key).getAddress(), msg);
			}
		}
	}
	
	public void removeObject(long key)
	{
		Message msg;
		if(state != STATE_CONNECTED || isBetween(key, predecessor.getId(), self.getId()))
		{
			System.out.println(self.getId() + ": removing data from own storage");
			datastore.removeEntry(key);
		} else
		{
			msg = new Message();
			msg.setKey(PROTOCOL_COMMAND, PROTOCOL_FIND_SUCCESSOR);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_KEY, key);
			msg.setKey(PROTOCOL_FIND_SUCCESSOR_COMMAND, PROTOCOL_FIND_SUCCESSOR_COMMAND_REMOVE);
			if(isBetween(key, self.getId(), successor.getId()))
			{
				System.out.println(self.getId() + ": passing removeObject-request to successor");
				send(successor.getAddress(), msg);
			} else
			{
				System.out.println(self.getId() + ": sending removeObject-request a long");
				send(ft.closestPrecedingNode(key).getAddress(), msg);
			}
		}
	}
	
	/* 
	 * returns true if l1 is in between l2 and l3 (clockwise) in a ring space OR l_1 == l_3
	 * second case due too the fact that we're looking for successors of values.
	 */
	public static boolean isBetween(long l_1, long l_2, long l_3)
	{
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
		else //shifted_l2 > 0 && shifted_l3 > 0
			return (shifted_l2 > shifted_l3);
	}
	
	public long getIdSpace() {
		return idSpace;
	}
	
	public int getAirtiy() {
		return arity;
	}
}

