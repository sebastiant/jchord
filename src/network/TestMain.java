package network;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class TestMain {
	
	public static void main(String[] args) throws Exception {
		MessageSender node1 = new MessageSender(9001);
		MessageSender node2 = new MessageSender(9002);
		
		node1.registerMessageObserver(new ConcreteObserver<Message>() {
			public void notifyObserver(Message e) {
				System.out.println("Recieved " + e.getKey("text") + " from " + e.getSourceAddress());
			}
		});
		node2.registerMessageObserver(new ConcreteObserver<Message>() {
			public void notifyObserver(Message e) {
				System.out.println("Recieved " + e.getKey("text") + " from " + e.getSourceAddress());
			}
		});
		node1.start();
		node2.start();
		
		Message msg = new Message();
		msg.setDestinationAddress(InetAddress.getByName("130.229.181.219").getHostAddress() + ":9001");
		msg.setKey("text", "Hello 1");
		node2.send(msg);
		
	}

}
