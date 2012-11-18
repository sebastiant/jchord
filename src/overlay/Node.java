package overlay;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import network.Address;
import network.ConcreteObserver;
import network.Message;
import network.MessageSender;
import network.events.ConnectionRefusedEvent;
import network.events.ControlEvent;
import network.events.DisconnectEvent;

public class Node {
	//Protocol-constants. Lookie lookie, no touchie!
	public static final String PROTOCOL_COMMAND = "comm";
	public static final String PROTOCOL_JOIN = "join";
	public static final String PROTOCOL_JOIN_ID = "joinid";
	public static final String PROTOCOL_JOIN_ARITY = "joinarity";
	public static final String PROTOCOL_JOIN_IDENTIFIERSPACE = "joinidspace";
	public static final String PROTOCOL_DISCONNECT = "disc";
	public static final String PROTOCOL_CLOSEDCONNECTION = "closed";
	public static final String PROTOCOL_DENIED = "denied";
	public static final String PROTOCOL_GRANTED = "granted";
	public static final String PROTOCOL_SUCCESSORINFORM = "succ";
	public static final String PROTOCOL_PREDECESSOR_RESPONSE = "pred";
	public static final String PROTOCOL_PREDECESSOR_REQUEST = "predreq";
	public static final String PROTOCOL_NULL = "null";
	
	public static final String STATE_DISCONNECTED = "disconnected";
	public static final String STATE_CONNECTED = "connected";
	public static final String STATE_CLOSEDCONNECTION = "closed";
	public static final String STATE_PREDECESSOR_REQUEST = "predreq";
	
	private MessageSender msgSender;
	private Map<Address, PeerEntry> peers = Collections.synchronizedMap(new HashMap<Address, PeerEntry>());
	
	private int localId;
	private String state;
	private PeerEntry predecessor;
	private PeerEntry successor;
	
	public Node(int port) {
		localId = port;
		predecessor = successor = null;
		state = null;
		peers = new HashMap<Address, PeerEntry>();
		msgSender = new MessageSender(port);
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
	}
	
	public void handleDisconnectEvent(DisconnectEvent e) {
		System.out.println("Received DisconnectEvent from some host!");
	}
	public void handleConnectionRefusedEvent(ConnectionRefusedEvent e) {
		System.out.println("Received ConnectionRefusedEvent when trying to connect to: " + e.getRemoteAddress());
	}
	public void handleMessage(Message msg) {
		Address src = msg.getSourceAddress();
		if(!msg.hasKey(PROTOCOL_COMMAND))
		{
			//Not following protocol, disregard it!
			handleUnknownMessage(src);
			return;
		}
		String command = (String) msg.getKey(PROTOCOL_COMMAND);
		System.out.println("Received message from: " + src);

		if(peers.get(msg.getSourceAddress()) != null){ //Connected node
			if(command.equals(PROTOCOL_JOIN)) //Expected when receiving back join ID
			{
				handleJoin(msg);
			} else if(command.equals(PROTOCOL_DISCONNECT)){
				handleDisconnect(src);
			} else if(command.equals(PROTOCOL_CLOSEDCONNECTION)){
				handleClosedConnection(src);
			} else if(command.equals(PROTOCOL_SUCCESSORINFORM)){
				handleSuccessorInform(msg);
			} else if(command.equals(PROTOCOL_PREDECESSOR_REQUEST)){
				handlePredecessorRequest(src);
			} else if(command.equals(PROTOCOL_PREDECESSOR_RESPONSE)){
				handlePredecessorResponse(msg);
			} else
			{
				handleUnknownMessage(src);
			}
		} else // peers.get(msg.getSourceAddress() == null
		{
			if(command.equals(PROTOCOL_JOIN))
			{
				handleJoin(msg);
			} else //Probably caused by churn -we've disconnected from the node due to timeout.
			{
				handleClosedConnection(src);
			}
		}
	}

	public void send(Address addr, Message msg){
		System.out.println("Sending msg: " + msg.getContent().toString() + ", to addr: " + addr.toString());
		msg.setDestinationAddress(addr.getInetAddress().getHostAddress() + ":" + addr.getPort());
		msgSender.send(msg);
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
     * @param Message The message containing the request with all parameters (wrapped JSONObject).
     * @return void
     */
	private void handleJoin(Message msg){
		Message response = new Message();
		Address src = msg.getSourceAddress();
		if(state.equals(STATE_CONNECTED)){
			System.out.println("Finally connected! :)");
		}
		else{
			System.out.println("Got join ");
			if(msg.hasKey(PROTOCOL_JOIN_ID)){
				peers.put(src, new PeerEntry(src, (Integer)msg.getKey(PROTOCOL_JOIN_ID)));
				response.setKey(PROTOCOL_COMMAND, PROTOCOL_JOIN);
				response.setKey(PROTOCOL_JOIN_ID, localId);
				if(state.equals(STATE_DISCONNECTED))
					state = STATE_CONNECTED;
			} else //Denied!
			{
				response.setKey(PROTOCOL_COMMAND, PROTOCOL_DENIED);
				send(src, response);
			}
		}
		send(src,response);
	}
	
	/**
     * Handle a received Disconnect message.
     * A Disconnect message is received when the sending node is disconnecting from the receiving.
     * Thus, the node must be removed from the set of active peers.
     * @param Address The address of the sending node
     * @return void
     */
	private void handleDisconnect(Address src){
		peers.remove(src);
		if(peers.isEmpty())
			state = STATE_DISCONNECTED;
	}
	
	/**
     * Handle a received SuccessorInform message.
     * A Successor inform is received when the sending node informs the receiving node that it is
     * its current successor. The receiving node compares its current successor with the sender
     * and makes a decision whether to change predecessor or not (done in silence, no information is sent out
     * regarding a possible change of predecessor).
     * @param Message The message containing the senders id.
     * @return void
     */
	private void handleSuccessorInform(Message msg){
		
	}
	
	/**
     * Handle a received PredecessorRequest message.
     * A PredecessorRequest is sent periodically by every node to its successor, to verify that the sending node
     * still is the receiving nodes predecessor. The receiving node simply responds by informing
     * who is its current predecessor. A change of one nodes predecessor is thus made without informing
     * the former predecessor, and the PredecessorRequest messages is the way of detecting it.
     * @param Address The address of the sending node
     * @return void
     */
	private void handlePredecessorRequest(Address src){
		
	}
	
	/**
     * Handle a received PredecessorResponse message.
     * A PredecessorResponse is received as a response to a PredecessorRequest message
     * and contains information of the sending nodes current predecessor.
     * The receiving node is interested in knowing whether or not it is the sending nodes predecessor.
     * @param Message The message containing the senders predecessor.
     * @return void
     */
	private void handlePredecessorResponse(Message msg){
		if(state.equals(STATE_PREDECESSOR_REQUEST))
		{
			
		}
	}
	
	/**
     * Handle a received ClosedConnection
     * A ClosedConnection message is received when the sender has closed the connection to the receiving
     * node. Instead of coping with partial disconnects in the system, this version of the protocol
     * simply disconnects from all nodes when receiving a ClosedConnection. This is to simplify connectivity
     * and states in the system, as if not doing this, nodes in the system can never form a full mesh.
     * @param Address The address of the sending node.
     * @return void
     */
	private void handleClosedConnection(Address src){
		if(!state.equals(STATE_DISCONNECTED))
		{
			//Disconnect from all connected nodes.
			state = STATE_DISCONNECTED;
			Message response = new Message();
			response.setKey(PROTOCOL_COMMAND, PROTOCOL_DISCONNECT);
			msgSender.sendToAll(response);
			peers.clear();
		}
	}
	
	/**
     * Handle a message with unknown syntax
     * Current version of the protocol just drops it and writes to stdout about it.
     * @param Message The message containing the senders id.
     * @return void
     */
	private void handleUnknownMessage(Address src){
		System.out.println("Received unknown message!");
		return;
	}
}
