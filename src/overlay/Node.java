package overlay;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import network.Address;
import network.ConcreteObserver;
import network.MessageSender;
import network.events.ConnectionRefusedEvent;
import network.events.ControlEvent;
import network.events.DisconnectEvent;
import network.events.Message;

public class Node implements Protocol {

	private MessageSender msgSender;
	private Map<Address, PeerEntry> peers = Collections.synchronizedMap(new HashMap<Address, PeerEntry>());
	
	private int arity;
	private long idSpace;
	private long localId;
	private String state;
	private PeerEntry predecessor;
	private PeerEntry successor;
	
	public Node(Address addr, long idSpace, int arity) {
		this.idSpace = idSpace;
		this.arity = arity;
		localId = IDGenerator.getId(addr, idSpace);
		predecessor = successor = null;
		state = STATE_DISCONNECTED;
		peers = new HashMap<Address, PeerEntry>();
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
	}
	
	public void shutdown()
	{
		msgSender.stop();
	}
	public void send(Address addr, Message msg){
		System.out.println("Sending msg: " + msg.toString() + ", to addr: " + addr.toString());
		msg.setDestinationAddress(addr.getInetAddress().getHostAddress() + ":" + addr.getPort());
		msgSender.send(msg);
	}

	public void sendToAll(Message msg){
		//TODO: implement me ;D
	}
	
	public void connect(Address addr)
	{
		state = STATE_CONNECTING;
		Message msg = new Message();
		msg.setDestinationAddress(addr.getInetAddress(), addr.getPort());
		msg.setKey(Node.PROTOCOL_COMMAND, Node.PROTOCOL_JOIN);
		msg.setKey(Node.PROTOCOL_JOIN_ARITY, arity);
		msg.setKey(Node.PROTOCOL_JOIN_IDENTIFIERSPACE, idSpace);
		msg.setKey(Node.PROTOCOL_JOIN_ID, localId);
		send(addr, msg);
		
	}
	public void handleDisconnectEvent(DisconnectEvent e) {
		System.out.println("Received DisconnectEvent from some host!");
		
	}
	
	public void handleConnectionRefusedEvent(ConnectionRefusedEvent e) {
		System.out.println("Received ConnectionRefusedEvent when trying to connect to: " + e.getRemoteAddress());
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
		System.out.println("Received message from: " + src);

		if(peers.get(msg.getSourceAddress()) != null){ //Connected node
			if(command.equals(PROTOCOL_JOIN))
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
				handleUnknownMessage(msg);
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
		Message response = new Message();
		Address src = msg.getSourceAddress();
		if(state.equals(STATE_CONNECTING))
		{
			if(msg.has(PROTOCOL_JOIN_ID))
			{
				System.out.println("Finally connected! :)");
				state = STATE_CONNECTED;
				//Set node as successor and predecessor
				peers.put(src, new PeerEntry(src, msg.getLong(PROTOCOL_JOIN_ID)));
				successor = predecessor = peers.get(src);
			}
		}
		else
		{
			System.out.println("Got join ");
			if(msg.has(PROTOCOL_JOIN_ID) && msg.has(PROTOCOL_JOIN_ARITY)
					&& msg.has(PROTOCOL_JOIN_IDENTIFIERSPACE))
			{
				if(((Integer)msg.getKey(PROTOCOL_JOIN_ARITY) == arity)
						&& ((Integer)msg.getKey(PROTOCOL_JOIN_IDENTIFIERSPACE) == idSpace))
				{
					//Accept the join!
					peers.put(src, new PeerEntry(src, msg.getLong(PROTOCOL_JOIN_ID)));
					response.setKey(PROTOCOL_COMMAND, PROTOCOL_JOIN);
					response.setKey(PROTOCOL_JOIN_ID, localId);
					response.setKey(PROTOCOL_JOIN_ARITY, arity);
					if(state.equals(STATE_DISCONNECTED))
					{
						state = STATE_CONNECTED;
						predecessor = successor = peers.get(src);
					}
					else //Update predecessor?
					{
						if(isBetween(msg.getLong(PROTOCOL_JOIN_ID), localId, 
								predecessor.getId()))
						{
							System.out.println("My Id: " + localId + ". Changing predecessor from "
									+ predecessor.getId() + ", to: " +(Long)msg.getLong(PROTOCOL_JOIN_ID));
						}
						predecessor = peers.get(src);
					}
					send(src,response);
					return;
				}
			}
			response.setKey(PROTOCOL_COMMAND, PROTOCOL_DENIED);
			send(src, response);
		}
	}
	
	/**
     * Handle a received Disconnect message.
     * A Disconnect message is received when the sending node is disconnecting from the receiving.
     * Thus, the node must be removed from the set of active peers.
     * @param src The address of the sending node
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
     * @param msg The message containing the senders id.
     * @return void
     */
	private void handleSuccessorInform(Message msg){
		if(!msg.has(PROTOCOL_SUCCESSORINFORM))
				return;
		if(!msg.getKey(PROTOCOL_SUCCESSORINFORM).equals(predecessor.getId())){
			//TODO: Change current predecessor?
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
		if(predecessor == null)
			response.setKey(PROTOCOL_PREDECESSOR_RESPONSE, PROTOCOL_NULL);
		else
			response.setKey(PROTOCOL_PREDECESSOR_RESPONSE, predecessor.getId());
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
		if(state.equals(STATE_DISCONNECTED))
			return;
		//Disconnect from all connected nodes.
		state = STATE_DISCONNECTED;
		Message response = new Message();
		response.setKey(PROTOCOL_COMMAND, PROTOCOL_DISCONNECT);
		sendToAll(response);
		peers.clear();
	}
	
	/**
     * Handle a message with unknown syntax
     * Current version of the protocol just drops it and writes to stdout about it.
     * @param msg The message containing the senders id.
     * @return void
     */
	private void handleUnknownMessage(Message msg){
		System.out.println("Received unknown message: " +msg.toString());
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
		return localId;
	}
	
	/* 
	 * returns true if l1 is in between l2 and l3 (clockwise) in a ring space.
	 */
	public static boolean isBetween(long l_1, long l_2, long l_3)
	{
		long shifted_l2 = l_2 - l_1;
		long shifted_l3 = l_3 - l_1;
		if((shifted_l2 < 0) && (shifted_l3 < 0))
			return (shifted_l2 < shifted_l3);
		else if(shifted_l3 < 0)
			return true;
		else if(shifted_l2 < 0)
			return false;
		else //shifted_l2 > 0 && shifted_l3 > 0
			return (shifted_l2 < shifted_l3);
	}
}
