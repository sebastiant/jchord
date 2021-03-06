package com.github.sebastiant.jchord.network;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.github.sebastiant.jchord.network.events.ConnectionRefusedEvent;
import com.github.sebastiant.jchord.network.events.ControlEvent;
import com.github.sebastiant.jchord.network.events.Message;

import org.junit.*;

import static org.junit.Assert.*;


public class TestConnectionRefused {
	
	MessageSender node1;
	boolean eventRecieved = false;
	
	@Before
	public void setUp() {
		node1 = new MessageSender(9201);
		node1.start();
	}
	
	@Test
	public void test() {
		node1.registerControlObserver(new ConcreteObserver<ControlEvent>() {
			@Override
			public void notifyObserver(ControlEvent e) {
				if(e instanceof ConnectionRefusedEvent) {
					ConnectionRefusedEvent ref = (ConnectionRefusedEvent)e;
					try {
						assertTrue(ref.getSource().getInetAddress().equals(InetAddress.getLocalHost()));
					} catch (UnknownHostException e1) {
						fail(e1.getMessage());
					}
					assertTrue(ref.getSource().getPort() == 9020);
					eventRecieved = true;
				}
			}
		});
		Message message = new Message();
		try {
			message.setDestinationAddress(new Address(InetAddress.getLocalHost(), 9020));
		} catch (UnknownHostException e1) {
			fail(e1.getMessage());
		}
		node1.send(message);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			fail(e1.getMessage());
		}
		assertTrue(eventRecieved);
	}
	
	@After
	public void cleanUp() {
		node1.stop();
		node1 = null;
	}

}
