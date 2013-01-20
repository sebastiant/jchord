package overlay;

import java.util.Timer;
import java.util.TimerTask;

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
	/* Default value, self */
	private PeerEntry predecessor;
	private PeerEntry successor;
	private PeerEntry successorlist[];
	private boolean running = true;
	private long predecessorLastSeen = Long.MAX_VALUE;
	
	public static final int PRED_REQ_INTERVAL = 500;
	public static final int FINGER_FIX_INTERVAL = 1000;

	private Timer checkPredecessorTimer = new Timer(true);
	private Timer checkFingersTimer = new Timer(true);
	
	public Node(Address addr, long idSpace, int arity) {
		this.idSpace = idSpace;
		this.arity = arity;
		long localId = IDGenerator.getId(addr, idSpace);
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
					sendCheckPredecessor();
					sendPredRequest();
				}
			}
			
		}, 1000, PRED_REQ_INTERVAL);
		
		checkFingersTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if(state == STATE_CONNECTED) {
					fixFingers();
				}
			}
		}, 1000, FINGER_FIX_INTERVAL); 
	}
	
	public void shutdown()
	{
		if(running) {
			checkFingersTimer.cancel();
			checkPredecessorTimer.cancel();
			msgSender.stop();
			running = false;
		}
	}
	
	public void send(Address addr, Message msg){
		msg.setDestinationAddress(addr);
	//	System.out.println("Sending msg: " + msg.toString() + ", to addr: " + addr);
		msgSender.send(msg);
	}
	
	public void sendToAll(Message msg){
		//TODO: implement me ;D
	}
	
	public void connect(Address addr)
	{
		state = STATE_CONNECTING;
		Message msg = new Message();
		msg.setKey(Node.PROTOCOL_COMMAND, Node.PROTOCOL_JOIN);
		msg.setKey(Node.PROTOCOL_JOIN_ARITY, arity);
		msg.setKey(Node.PROTOCOL_JOIN_IDENTIFIERSPACE, idSpace);
		msg.setKey(Node.PROTOCOL_JOIN_ID, self.getId());
		send(addr, msg);
		
	}
	
	private void sendCheckPredecessor() {
		long now = System.currentTimeMillis();
		if(predecessor != null) {
			Message msg = new Message();
			msg.setKey(Node.PROTOCOL_COMMAND, PROTOCOL_CHECK_PREDECESSOR);
			send(predecessor.getAddress(), msg);
			if(predecessorLastSeen - now > PRED_REQ_INTERVAL*2) {
				predecessor = null;
				System.err.println(self + ": Predecessor timed out");
			} 
		}
	}
	
	private void updatePredecessor(PeerEntry predecessor) {
		predecessorLastSeen = System.currentTimeMillis();
		if(this.predecessor == null)
		{
			System.out.println("(" + self.getId() + ") changing predecessor from: NULL to: " + predecessor.getId());
		}
		else{
			System.out.println("(" + self.getId() + ") changing predecessor from: " + this.predecessor.getId() +" to: " + predecessor.getId());

		}
		this.predecessor = predecessor;
	}
	public void updateSuccessor(PeerEntry successor)
	{
		if(this.successor == null)
		{
			System.out.println("(" + self.getId() + ") changing successor from: NULL to: " + successor.getId());
		}
		else{
			System.out.println("(" + self.getId() + ") changing successor from: " + this.successor.getId() +" to: " + successor.getId());

		}
		this.successor = successor;
		successorlist[0] = successor;
	}
	private void sendPredRequest() {
		if(!successor.equals(self)) {
			Message msg = new Message();
			msg.setKey(Node.PROTOCOL_COMMAND, Node.PROTOCOL_PREDECESSOR_REQUEST);
			send(successor.getAddress(), msg);
		}
	}
	
	private void sendSuccessorInform() {
		if(!self.equals(successor)) {
			Message msg = new Message();
			msg.setKey(Node.PROTOCOL_COMMAND, Node.PROTOCOL_SUCCESSORINFORM);
			msg.setKey(Node.PROTOCOL_SENDER_ID, self.getId());
			send(successor.getAddress(), msg);
		}
	}
	
	public void handleDisconnectEvent(DisconnectEvent e) {
		System.out.println("ID("+self.getId()+") Received DisconnectEvent from some host!");
	}
	
	public void handleConnectionRefusedEvent(ConnectionRefusedEvent e) {
		System.out.println("ID("+self.getId()+") Received ConnectionRefusedEvent when trying to connect to: " + e.getSource());
		if(e.getSource().equals(successor.getAddress()))
		{
			System.out.println("ID("+self.getId()+") My successor has disconnected!");
			successor = self;
			successorlist[0] = null;
			successorlist[1] = null;
			successorlist[2] = null;
		}
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
		//System.out.println("ID("+self.getId()+") Received command " + command + " from: " + src);

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
		}else
		{
			handleUnknownMessage(msg);
		}
	}

	/** Respond on predecessor pings */
	public void handleCheckPredecessor(Message msg) {
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
			System.out.println("ID("+self.getId()+") Received join request although im connecting myself. dropped.");
			return;
		}
		Address src = msg.getSourceAddress();
		Message resp = new Message();
		System.out.println("ID("+self.getId()+") Received join request ");
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
					resp.setKey(Node.PROTOCOL_COMMAND, Node.PROTOCOL_FIND_SUCCESSOR_RESPONSE);
					resp.setKey(Node.PROTOCOL_FIND_SUCCESSOR_KEY, msg.getLong(PROTOCOL_JOIN_ID));
					resp.setKey(Node.PROTOCOL_FIND_SUCCESSOR_RESPONSE_ADDR, self.getAddress().toString());
					resp.setKey(Node.PROTOCOL_FIND_SUCCESSOR_RESPONSE_ID, self.getId());
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
			System.out.println("ID("+self.getId()+") My connection was DENIED by (" + msg.getLong(PROTOCOL_JOIN_ID));
			state = STATE_DISCONNECTED;
		}
		else
		{
			System.out.println("ID("+self.getId()+") Got join denied even though im not connecting!");
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
		long sender = msg.getLong(Node.PROTOCOL_SENDER_ID);
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
			sendSuccessorInform();
			successorlist[0] = null;
			successorlist[1] = null;
			successorlist[2] = null;
		}
	}
	
	/**
     * Handle a received ClosedConnection
     * A ClosedConnection message is received when the sender has closed the connection to the receiving
     * node. Instead of coping with partial disconnects in the system, this version of the protocol
     * simply disconnects from all nodes when receiving a ClosedConnection. This is to simplify connectivity
     * and states in the system, as if not doing this, nodes in the system can never form a full mesh.
     * @param src The address of the sending node.
     * @return void
     */
	private void handleClosedConnection(Address src){
		System.out.println("ID("+self.getId()+") handling closed connection!!");
	}
	/**
     * Handle a received FindSuccessor
     * @param src The address of the sending node.
     * @return void
     */
	private void handleFindSuccessor(Message msg){
		if(msg.getSourceAddress().equals(self.getAddress()))
		{
			System.out.println("!!! ID("+self.getId()+") Received findsuccessor from self");
			return;
		}
		if(successor == null || successor == self)
		{
			System.out.println("!!! ID("+self.getId()+") Cant handle findsuccessor, have no successor.");
			return;
		}
		if(isBetween(msg.getLong(Node.PROTOCOL_FIND_SUCCESSOR_KEY),self.getId(), successor.getId()))
		{
			Message resp = new Message();
			resp.setKey(Node.PROTOCOL_COMMAND, Node.PROTOCOL_FIND_SUCCESSOR_RESPONSE);
			resp.setKey(Node.PROTOCOL_FIND_SUCCESSOR_KEY, msg.getLong(Node.PROTOCOL_FIND_SUCCESSOR_KEY));
			resp.setKey(Node.PROTOCOL_FIND_SUCCESSOR_RESPONSE_ADDR, successor.getAddress().toString());
			resp.setKey(Node.PROTOCOL_FIND_SUCCESSOR_RESPONSE_ID, successor.getId());
			send(new Address(msg.getString(Node.PROTOCOL_FIND_SUCCESSOR_SENDER_ADDR)), resp);
			return;
		}
		
		long key = msg.getLong(Node.PROTOCOL_FIND_SUCCESSOR_KEY);
		if(ft.closestPrecedingNode(key).getId() == self.getId())
		{
			Message resp = new Message();
			resp.setKey(Node.PROTOCOL_COMMAND, Node.PROTOCOL_FIND_SUCCESSOR_RESPONSE);
			resp.setKey(Node.PROTOCOL_FIND_SUCCESSOR_KEY, msg.getLong(Node.PROTOCOL_FIND_SUCCESSOR_KEY));
			resp.setKey(Node.PROTOCOL_FIND_SUCCESSOR_RESPONSE_ADDR, self.getAddress().toString());
			resp.setKey(Node.PROTOCOL_FIND_SUCCESSOR_RESPONSE_ID, self.getId());
			send(new Address(msg.getString(Node.PROTOCOL_FIND_SUCCESSOR_SENDER_ADDR)), resp);
			return;
		}
		else
		{
			/*
			 System.out.println("!!! ID("+self.getId()+") sending closest preceding nodes. ID: "
					+ ft.closestPrecedingNode(key).getId()
					+ "and key is: " + key);
					*/
			send(ft.closestPrecedingNode(key).getAddress(), msg);
		}
	}

	/**
     * Handle a received FindSuccessorResponse
     * @param src The address of the sending node.
     * @return void
     */
	private void handleFindSuccessorResponse(Message msg){
		//System.out.println("ID("+self.getId()+") Received find successor response");
		if(state == Node.STATE_CONNECTING)
		{
			if(msg.getLong(PROTOCOL_FIND_SUCCESSOR_KEY) == self.getId())
			{
				updateSuccessor(new PeerEntry(new Address(msg.getString(PROTOCOL_FIND_SUCCESSOR_RESPONSE_ADDR)),
						msg.getInt(PROTOCOL_FIND_SUCCESSOR_RESPONSE_ID)));
				state = Node.STATE_CONNECTED;
				System.out.println("("+self.getId()+") Connection Accepted! Successor set to: " + successor.getId());
			}
		}
		PeerEntry pe = new PeerEntry(new Address(msg.getString(Node.PROTOCOL_FIND_SUCCESSOR_RESPONSE_ADDR)),
				msg.getLong(Node.PROTOCOL_FIND_SUCCESSOR_RESPONSE_ID));
		ft.setFingerEntry(msg.getLong(Node.PROTOCOL_FIND_SUCCESSOR_KEY), pe);
	}
	/**
     * Handle a message with unknown syntax
     * Current version of the protocol just drops it and writes to stdout about it.
     * @param msg The message containing the senders id.
     * @return void
     */
	private void handleUnknownMessage(Message msg){
		System.out.println("!!! ID("+self.getId()+")Received unknown message: " +msg.toString());
		return;
	}
	
	
	/*
	 * Methods to support testing.
	 */
	
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
	
	public void fixFingers()
	{
		for(FingerEntry e : ft.getEntries())
		{
			if(e.getPeerEntry() != null)
			{
				findSuccessor(e.getKey());
			}
			
		}
		/*
		for(FingerEntry e : ft.getEntries()){
			System.out.println("("+self.getId()+")Entry: " + e.getKey() + " -> " + e.getPeerEntry().getId());
		}
*/
	}
	
	/* Recursive ring lookup */
	public void findSuccessor(long key)
	{
		Message msg = new Message();
		msg.setKey(Node.PROTOCOL_COMMAND, Node.PROTOCOL_FIND_SUCCESSOR);
		msg.setKey(Node.PROTOCOL_FIND_SUCCESSOR_KEY, key);
		msg.setKey(Node.PROTOCOL_FIND_SUCCESSOR_SENDER_ADDR, self.getAddress().toString());
		if(isBetween(key,self.getId(), successor.getId()))
		{
			ft.setFingerEntry(key, successor);
		}
		else
		{
			//System.out.println("("+self.getId()+") Sending findsuccessor to: (" + ft.closestPrecedingNode(key).getId() + ")");
			if(ft.closestPrecedingNode(key).equals(self))
			{
				ft.setFingerEntry(key, self);
			} else
			{
				send(ft.closestPrecedingNode(key).getAddress(), msg);
			}
		}
	}
	
	private void findSuccessorForNode(PeerEntry source){
		if(successor == null)
		{
			return;
		}
		if(isBetween(source.getId(),self.getId(), successor.getId()))
		{
			Message resp = new Message();
			resp.setKey(Node.PROTOCOL_COMMAND, Node.PROTOCOL_FIND_SUCCESSOR_RESPONSE);
			resp.setKey(Node.PROTOCOL_FIND_SUCCESSOR_KEY, source.getId());
			resp.setKey(Node.PROTOCOL_FIND_SUCCESSOR_RESPONSE_ADDR, source.getAddress().toString());
			resp.setKey(Node.PROTOCOL_FIND_SUCCESSOR_RESPONSE_ID, source.getId());
			send(successor.getAddress(), resp);
		}
		else
		{
			Message msg = new Message();
			msg.setKey(Node.PROTOCOL_COMMAND, Node.PROTOCOL_FIND_SUCCESSOR);
			msg.setKey(Node.PROTOCOL_FIND_SUCCESSOR_KEY, source.getId());
			msg.setKey(Node.PROTOCOL_FIND_SUCCESSOR_SENDER_ADDR, source.getAddress().toString());
			send(ft.closestPrecedingNode(source.getId()).getAddress(), msg);
		}
	}
	
	public FingerEntry[] getFingers() {
		return ft.getEntries();
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
}
