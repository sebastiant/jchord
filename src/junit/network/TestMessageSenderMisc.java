package junit.network;
import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import network.Address;
import network.ConcreteObserver;
import network.MessageSender;
import network.events.Message;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class TestMessageSenderMisc {
	
	boolean startRcvMsg;
	MessageSender node1, node2, node3, node4, node5, node6, node7, node8, node9;
	@Before
	public void setUp() {
		node1 = new MessageSender(9401);
		node2 = new MessageSender(9402);
		node3 = new MessageSender(9403);
		node4 = new MessageSender(9404);
	}

	@Test
	public void testStart() {
		startRcvMsg = false;
		node2.registerMessageObserver(new ConcreteObserver<Message>() {
			@Override
			public void notifyObserver(Message e) {
				assertTrue(e.has("content"));
				assertEquals(e.getString("content"), "message1");
				startRcvMsg = true;
			}		
		});
		// try starting twice
		node2.start();
		node2.start();
		Message message = new Message();
		message.setKey("content", "message1");
		message.setDestinationAddress(node2.getAddress());
		// send before started
		assertFalse(node1.send(message));	
		assertFalse(startRcvMsg);
		node1.start();
		assertTrue(node1.send(message));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			fail(e1.getMessage());
		}
		assertTrue(startRcvMsg);
	}
	
	@Test
	public void testDisconnect() {
		node1.start();
		node2.start();
		Message msg = new Message();
		msg.setDestinationAddress(node2.getAddress());
		node1.send(msg);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			fail(e1.getMessage());
		}
		assertTrue(node1.getConnections().size() == 1);
		Address addr = node1.getConnections().entrySet().
							iterator().next().getValue().getAddress();
		assertNotNull(addr);
		System.out.println(addr);
		assertTrue(node1.disconnect(addr));
		assertFalse(node1.getConnections().contains(addr));
		assertTrue(node1.getConnections().size() == 0);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
		//assertTrue(node2.getConnections().size() == 0);
		assertFalse(node1.disconnect(addr));
	}

	@Test
	public void testGetConnections() {
		node1.start();
		node2.start();
		node3.start();
		node4.start();
		Message msg = new Message();
		msg.setDestinationAddress(node1.getAddress());
		node2.send(msg);
		node3.send(msg);
		node4.send(msg);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			fail(e1.getMessage());
		}
		assertTrue(node1.getConnections().size() == 3);
		assertTrue(node1.getConnections().containsKey(node2.getAddress()));
		assertTrue(node1.getConnections().containsKey(node3.getAddress()));
		assertTrue(node1.getConnections().containsKey(node4.getAddress()));

		assertTrue(node2.getConnections().containsKey(node1.getAddress()));
		assertEquals(node2.getConnections().get(node1.getAddress()).getAddress(),node1.getAddress());

	}

	@Test
	public void testGetAddress() {
		assertTrue(node1.getAddress().getPort() == 9401);
		try {
			assertEquals(node1.getAddress().getInetAddress(), InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			fail(e.getMessage());
		}
	}
	
	@After
	public void cleanUp() {
		node1.stop();
		node2.stop();
		node3.stop();
		node4.stop();
		node1 = node2 = node3 = node4 = null;
	}

}
