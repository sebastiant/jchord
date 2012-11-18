package network;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.json.JSONException;

import network.events.ConnectionRefusedEvent;
import network.events.ControlEvent;
import network.events.DisconnectEvent;
import network.events.Message;

public class TestMain {
	
	public static void main(String[] args) throws Exception {
		MessageSender node1 = new MessageSender(9001);
		MessageSender node2 = new MessageSender(9002);
		MessageSender node3 = new MessageSender(9002);
		ConcreteObserver<ControlEvent> controlObs = new ConcreteObserver<ControlEvent>() {
			@Override
			public void notifyObserver(ControlEvent e) {
				if(e instanceof DisconnectEvent) {
					DisconnectEvent ds = (DisconnectEvent)e;
					System.out.println("Disconnect event from " + ds.getSource());
				} else if (e instanceof ConnectionRefusedEvent) {
					ConnectionRefusedEvent ce = (ConnectionRefusedEvent)e;
					System.out.println("Connection to " + ce.getRemoteAddress() + " was refused!");
				}
 			}	
		};
		ConcreteObserver<Message> messageObs = new ConcreteObserver<Message>() {
			@Override
			public void notifyObserver(Message e) {
					try {
					String text = e.getString("text");
					Address address = e.getSourceAddress();
					System.out.println("Receved \"" + text + "\" from " + address.toString());
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}	
		};
		
		node1.registerMessageObserver(messageObs);
		node2.registerMessageObserver(messageObs);
		node3.registerMessageObserver(messageObs);
		node1.registerControlObserver(controlObs);
		node2.registerControlObserver(controlObs);
		node3.registerControlObserver(controlObs);
		
		node1.start();
		node2.start();
		node2.stop();
		node3.start();
		
		Message msg = new Message();
		msg.setDestinationAddress(InetAddress.getByName("localhost").getHostAddress() + ":9001");
		msg.put("text", "Hello 1");
		node2.send(msg);
		Message msg2 = new Message();
		msg2.setDestinationAddress(InetAddress.getByName("localhost").getHostAddress() + ":9002");
		msg2.put("text", "Hello 2");
		node1.send(msg2);
		Message msg3 = new Message();
		msg3.setDestinationAddress(InetAddress.getByName("localhost").getHostAddress() + ":9099");
		msg3.put("text", "Hello 3");
		node3.send(msg3);
	}

}
