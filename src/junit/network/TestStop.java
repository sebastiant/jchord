package junit.network;

import static org.junit.Assert.*;
import network.ConcreteObserver;
import network.MessageSender;
import network.events.Message;

import org.junit.*;

public class TestStop {
	MessageSender node1;
	MessageSender node2;
	MessageSender node3;
	
	boolean recievedMessage1 = false;
	boolean recievedMessage2 = false;
	boolean recievedMessage4 = false;
	
	@Before
	public void setUp() {
		node1 = new MessageSender(9001);
		node2 = new MessageSender(9002);
		node3 = new MessageSender(9003);
		node1.start();
		node2.start();
		node3.start();
	}
	
	@Test
	public void test() {
		node2.registerMessageObserver(new ConcreteObserver<Message>() {
			@Override
			public void notifyObserver(Message e) {
				if(e.has("content") && e.getString("content").equals("message1")) {
					recievedMessage1 = true;
				}
				if(e.has("content") && e.getString("content").equals("message3")) {
					fail("Recieved message while being in stopped state.");
				}
				
			}	
		});
		node3.registerMessageObserver(new ConcreteObserver<Message>() {
			@Override
			public void notifyObserver(Message e) {
				if(e.has("content") && e.getString("content").equals("message2")) {
					recievedMessage2 = true;
				}
				if(e.has("content") && e.getString("content").equals("message4")) {
					recievedMessage4 = true;
				}
				if(e.has("content") && e.getString("content").equals("message5")) {
					fail("Recieved message from stopped node");
				}
			}	
		});
		
		Message message = new Message();
		message.setDestinationAddress(node2.getAddress());
		message.setKey("content", "message1");
		node1.send(message);
		message = new Message();
		message.setDestinationAddress(node3.getAddress());
		message.setKey("content", "message2");
		node1.send(message);
		node2.stop();
		message = new Message();
		message.setDestinationAddress(node2.getAddress());
		message.setKey("content", "message3");
		node1.send(message);
		message = new Message();
		message.setDestinationAddress(node3.getAddress());
		message.setKey("content", "message4");
		node1.send(message);
		message = new Message();
		message.setDestinationAddress(node3.getAddress());
		message.setKey("content", "message5");
		assertFalse(node2.send(message));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			fail(e1.getMessage());
		}
		assertTrue(recievedMessage1);
		assertTrue(recievedMessage2);
		assertTrue(recievedMessage4);
	}
	
	@After
	public void cleanUp() {
		node1.stop();
		node2.stop();
		node3.stop();
		node1 = null;
		node2 = null;
		node3 = null;
	}
}
