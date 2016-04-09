package com.github.sebastiant.jchord.junit.network;


import java.net.InetAddress;
import java.net.UnknownHostException;

import com.github.sebastiant.jchord.network.Address;
import com.github.sebastiant.jchord.network.ConcreteObserver;
import com.github.sebastiant.jchord.network.MessageSender;
import com.github.sebastiant.jchord.network.events.Message;

import static org.junit.Assert.*;
import org.junit.*;

public class TestSend {
	MessageSender node1;
	MessageSender node2;
	boolean gotMessage1 = false;
	boolean gotMessage2 = false;

	@Before
	public void setUp() {
		node1 = new MessageSender(9301);
		node2 = new MessageSender(9302);
		node1.start();
		node2.start();
	}
	
	@Test
	public void test() {
		node1.registerMessageObserver(new ConcreteObserver<Message>() {
			@Override
			public void notifyObserver(Message m) {
				assertTrue(m.getSourceAddress().getPort() == 9302);
				assertTrue(m.has("message"));
				assertTrue(m.getString("message").equals("hello2"));
				gotMessage2 = true;
			}
		});
		
		node2.registerMessageObserver(new ConcreteObserver<Message>() {
			@Override
			public void notifyObserver(Message m) {
				assertTrue(m.getSourceAddress().getPort() == 9301);
				assertTrue(m.has("message"));
				assertTrue(m.getString("message").equals("hello1"));
				gotMessage1 = true;
			}
		});
		Message message = new Message();
		message.setKey("message","hello1");
		try {
			message.setDestinationAddress(new Address(InetAddress.getLocalHost(), 9302));
		} catch (UnknownHostException e) {
			fail(e.getMessage());
		}
		assertTrue(node1.send(message));
		
		message = new Message();
		message.setKey("message","hello2");
		try {
			message.setDestinationAddress(new Address(InetAddress.getLocalHost(), 9301));
		} catch (UnknownHostException e) {
			fail(e.getMessage());
		}
		assertTrue(node2.send(message));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(gotMessage1);
		assertTrue(gotMessage2);
		
		// Only one connection was created
		assertTrue(node1.getConnections().size() == 1);
		assertTrue(node2.getConnections().size() == 1);
	}
	
	@After
	public void cleanUp() {
		node1.stop();
		node2.stop();
		node1 = null;
		node2 = null;
	}	
}
