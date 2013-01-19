package network;

import java.net.InetAddress;

import network.events.ConnectionRefusedEvent;
import network.events.ControlEvent;
import network.events.DisconnectEvent;
import network.events.Message;

public class TestMain {
	
	public static void main(String[] args) throws Exception {
		final MessageSender node1 = new MessageSender(9001);
		final MessageSender node2 = new MessageSender(9002);
		ConcreteObserver<ControlEvent> controlObs = new ConcreteObserver<ControlEvent>() {
			@Override
			public void notifyObserver(ControlEvent e) {
				if(e instanceof DisconnectEvent) {
					DisconnectEvent ds = (DisconnectEvent)e;
					System.out.println("Disconnect event from " + ds.getSource());
				} else if (e instanceof ConnectionRefusedEvent) {
					ConnectionRefusedEvent ce = (ConnectionRefusedEvent)e;
					System.out.println("Connection to " + ce.getSource() + " was refused!");
				}
 			}	
		};
		ConcreteObserver<Message> messageObs = new ConcreteObserver<Message>() {
			@Override
			public void notifyObserver(Message e) {
					String text = e.getString("text");
					Address address = e.getSourceAddress();
					System.out.println("Receved \"" + text + "\" from " + address.toString());
				
			}	
		};
		node1.registerMessageObserver(messageObs);
		node2.registerMessageObserver(messageObs);
		node1.registerControlObserver(controlObs);
		node2.registerControlObserver(controlObs);
		
		node1.start();
		node2.start();
		
		Message msg = new Message();
		msg.setDestinationAddress(InetAddress.getByName("localhost").getHostAddress() + ":9002");
		msg.setKey("text", "Hello 1");
		node1.send(msg);
		//node2.stop();
		msg = new Message();
		msg.setDestinationAddress(InetAddress.getByName("localhost").getHostAddress() + ":9001");
		msg.setKey("text", "Hello 2");
		node2.send(msg);
		
		/*Timer t1 = new Timer();
		Timer t2 = new Timer();
		Calendar cl = Calendar.getInstance();
		cl.add(Calendar.SECOND, 5);
		Date time = cl.getTime();
		Date time2 = cl.getTime();*/
	/*	t1.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					Message msg = new Message();
					msg.setDestinationAddress(InetAddress.getByName("localhost").getHostAddress() + ":9002");
					msg.setKey("text", "Hello 1");
					node1.send(msg);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
			}
		}, time);
		
		t2.schedule(new TimerTask() {
			@Override
			public void run() {	
				try {
					Message msg2 = new Message();
					msg2.setDestinationAddress(InetAddress.getByName("localhost").getHostAddress() + ":9001");
					msg2.setKey("text", "Hello 2");
					node2.send(msg2);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}	
		}, time2); */
		Thread.sleep(5*1000);
		node1.printConnections();
		node2.printConnections();
		
	}
}
