package overlay;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import network.Address;
import network.ConcreteObserver;
import network.ControlEvent;
import network.DisconnectEvent;
import network.Message;
import network.MessageSender;

public class Node {
	
	private MessageSender mySender;
	private HashMap<BigInteger, PeerEntry> peers;
	
	public Node(int port) {
		peers = new HashMap<BigInteger, PeerEntry>();
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
					handleDisconnectEvent(e);
			}	
		});
		mySender.start();
	}
	
	public void handleDisconnectEvent(ControlEvent e) {
		System.out.println("Received DisconnectEvent from some host!");
	}
	public void handleMessage(Message msg) {
		System.out.println("Received Message: " + msg.getKey("text") + " from: " + msg.getSourceAddress());
	}
	public void send(Message msg) {
		mySender.send(msg);
	}
}
