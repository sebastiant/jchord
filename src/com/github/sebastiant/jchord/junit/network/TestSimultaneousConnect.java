package com.github.sebastiant.jchord.junit.network;
import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.github.sebastiant.jchord.network.ConcreteObserver;
import com.github.sebastiant.jchord.network.MessageSender;
import com.github.sebastiant.jchord.network.events.Message;

import org.junit.*;

public class TestSimultaneousConnect {
	MessageSender node1;
	MessageSender node2;
	Timer t1;
	Timer t2;
	Object signal;
	boolean receivedMessage1 = false;
	boolean receivedMessage2 = false;
	
	@Before
	public void setUp() {
		signal = new Object();
		node1 = new MessageSender(8001);
		node2 = new MessageSender(8002);
		node1.start();
		node2.start();
		t1 = new Timer();
		t2 = new Timer();
	}
	
	@Test
	public void Test() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, 1);
		Date date = cal.getTime();
		t1.schedule(new TimerTask() {
			@Override
			public void run() {
				System.out.println("Sending message 1");
				Message msg = new Message();	
				msg.setDestinationAddress(node2.getAddress());
				msg.setKey("content", "message1");
				node1.send(msg);
				System.out.println("Message 1 sent");
			}
			
		}, date);
		t2.schedule(new TimerTask() {
			@Override
			public void run() {
				System.out.println("Sending message 2");
				Message msg = new Message();
				msg.setDestinationAddress(node1.getAddress());
				msg.setKey("content", "message2");
				node2.send(msg);
				System.out.println("Message 2 sent");
			}		
		}, date);	
		node1.registerMessageObserver(new ConcreteObserver<Message> () {
			@Override
			public void notifyObserver(Message e) {
				if(e.has("content") && e.getString("content").equals("message2")) {
					receivedMessage1 = true;
					synchronized(signal) {
						signal.notifyAll();
					}
				}
			}
		});
		node2.registerMessageObserver(new ConcreteObserver<Message> () {
			@Override
			public void notifyObserver(Message e) {
				if(e.has("content") && e.getString("content").equals("message1")) {
					receivedMessage2 = true;
					synchronized(signal) {
						signal.notifyAll();
					}
				}
			}
		});
		long timeout = System.currentTimeMillis() + 
				(MessageSender.MAX_BACKOFF * MessageSender.MAX_ATTEMPTS);
		
		// Wait for timers to fire
		try {
			synchronized(signal){
				while(!(receivedMessage1 && receivedMessage2) && 
						(timeout - System.currentTimeMillis() > 0))
				{		
					signal.wait(timeout - System.currentTimeMillis());
				}
			}
		} catch (InterruptedException e1) {
			fail(e1.getMessage());
		}
		assertTrue(receivedMessage1);
		assertTrue(receivedMessage2);		
		assertTrue(node1.getConnections().size() == 1);
		assertTrue(node1.getConnections().size() == 1);
		assertTrue(node1.getConnections().entrySet().iterator().next().getValue().getPort()
				   == node2.getConnections().entrySet().iterator().next().getValue().getLocalPort());
		assertTrue(node1.getConnections().entrySet().iterator().next().getValue().getLocalPort()
				   == node2.getConnections().entrySet().iterator().next().getValue().getPort());
	}
	
	@After
	public void cleanUp() {
		node1.stop();
		node2.stop();
		node1 = node2 = null;
		signal = null;
		t1 = t2 = null;
	}
	

}
