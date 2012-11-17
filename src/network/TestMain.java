package network;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class TestMain {
	
	public static void main(String[] args) throws Exception {
		MessageSender node1 = new MessageSender(9001);
		MessageSender node2 = new MessageSender(9002);
		
		node1.register(new ConcreteObserver<Message>() {
			public void notifyObserver(Message e) {
				System.out.println("Recieved " + e.getKey("text") + " from " + e.getSourceAddress());
			}
		});
		node1.start();
		node2.start();
		
		Message msg = new Message();
		msg.setDestinationAddress(InetAddress.getByName("localhost").getHostAddress() + ":9001");
		msg.setKey("text", "Hello 1");
		node2.send(msg);
		msg.setDestinationAddress("127.0.0.1" + ":9009");
		msg.setKey("text", "Hello 2");
		node2.send(msg);
	}

}
