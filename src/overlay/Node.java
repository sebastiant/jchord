package overlay;


import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

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
	public static final String PROTOCOL_DISCONNECT = "disc";
	public static final String PROTOCOL_CLOSEDCONNECTION = "closed";
	public static final String PROTOCOL_DENIED = "denied";
	public static final String PROTOCOL_GRANTED = "granted";
	public static final String PROTOCOL_SUCCESSOR = "succ";
	public static final String PROTOCOL_PREDECESSOR = "pred";
	public static final String PROTOCOL_PREDECESSOR_REQUEST = "predreq";
	public static final String PROTOCOL_NULL = "null";
	
	private MessageSender mySender;
	private Map<Address, PeerEntry> peers = Collections.synchronizedMap(new HashMap<Address, PeerEntry>());
	
	private PeerEntry predecessor;
	private PeerEntry successor;
	
	public Node(int port) {
		predecessor = successor = null;
		peers = new HashMap<Address, PeerEntry>();
		mySender = new MessageSender(port);
		mySender.registerMessageObserver(new ConcreteObserver<Message>() {
			@Override
			public void notifyObserver(Message msg) {
				handleMessage(msg);
			}	
		});
		mySender.registerControlObserver(new ConcreteObserver<ControlEvent>() {
			@Override
			public void notifyObserver(ControlEvent e) {
				if(e instanceof DisconnectEvent)
					handleDisconnectEvent((DisconnectEvent) e);
				if(e instanceof ConnectionRefusedEvent)
					handleConnectionRefusedEvent((ConnectionRefusedEvent) e);
			}	
		});
		mySender.start();
	}
	
	public void handleDisconnectEvent(DisconnectEvent e) {
		System.out.println("Received DisconnectEvent from some host!");
	}
	public void handleConnectionRefusedEvent(ConnectionRefusedEvent e) {
		System.out.println("Received ConnectionRefusedEvent when trying to connect to: " + e.getRemoteAddress());
	}
	public void handleMessage(Message msg) {
		if(!msg.hasKey(PROTOCOL_COMMAND))
		{
			//Not following protocol, disregard it!
			System.out.println("Received unknown message!");
			return;
		}
		String command = (String) msg.getKey(PROTOCOL_COMMAND);
		Address src = msg.getSourceAddress();
		System.out.println("Received message from: " + src + " with id: " + msg.getId());
		Message response = new Message();
		if(peers.get(msg.getSourceAddress()) != null){ //Connected node
			if(command.equals(PROTOCOL_JOIN)) //Not expected!
			{
				
			} else if(command.equals(PROTOCOL_DISCONNECT)){
				
			} else if(command.equals(PROTOCOL_DENIED)){
				
			} else if(command.equals(PROTOCOL_SUCCESSOR)){
				
			} else if(command.equals(PROTOCOL_PREDECESSOR)){
				
			} else if(command.equals(PROTOCOL_PREDECESSOR_REQUEST)){
				
			}
		} else //New connection
		{
			if(command.equals(PROTOCOL_JOIN))
			{
				if(msg.hasKey(PROTOCOL_JOIN_ID)){
					peers.put(src, new PeerEntry(src, Integer.parseInt((String)msg.getKey(PROTOCOL_JOIN_ID))));
					response.setKey(PROTOCOL_COMMAND, PROTOCOL_GRANTED);
					send(src, response);
				} else //Denied!
				{
					response.setKey(PROTOCOL_COMMAND, PROTOCOL_DENIED);
					send(src, response);
				}
			} else //Probably caused by churn -we've disconnected from the node due to timeout.
			{
				response.setKey(PROTOCOL_COMMAND, PROTOCOL_CLOSEDCONNECTION);
				send(src, response);
			}
		}
	}
	public void send(Address addr, Message msg) {
		msg.setDestinationAddress(addr.getInetAddress().getHostAddress() + ":" + addr.getPort());
		mySender.send(msg);
	}
}
