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
	
	private MessageSender mySender;
	private Map<Address, PeerEntry> peers = Collections.synchronizedMap(new HashMap<Address, PeerEntry>());
	
	public Node(int port) {
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
		System.out.println("Received Message: " + msg.getKey("text") + " from: " + msg.getSourceAddress() + " with id: "
				+ msg.getId());
		if(peers.get(msg.getSourceAddress()) == null){
			
		}
			
	}
	public void send(Message msg) {
		mySender.send(msg);
	}
}
