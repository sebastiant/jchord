package com.github.sebastiant.jchord.network;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.github.sebastiant.jchord.network.events.ConnectionRefusedEvent;
import com.github.sebastiant.jchord.network.events.ControlEvent;
import com.github.sebastiant.jchord.network.events.Message;

/**
 * 
 * @author Jakob Stengård
 *
 * This class sends messages over tcp. The underlying tcp connections is effectively hidden from the user.
 * When a message sender is created, a tcp server is started on the given port to accept incoming connections.
 * At the first time a message is sent, the connection table is first consulted, if no connection
 * to the target host exists, a new connection is created, and the message is sent using that connection.
 * Otherwise, an existing connection is used.
 * 
 * When a message arrives on a connection, a Message object, containing the message is sent to all 
 * participants that have registered to receive messages from this MessageSender.
 * Control events are sent when connections are lost, or when a connection to a host is refused.
 * 
 * To prevent duplicate connections, incoming connections are denied while a connection
 * is being set up to another host. In this case, the refused host backs off for a random
 * timeout before trying to connect again, unless a connection has already been established.
 */

public class MessageSender {
	
	private Server server;
	private ConcurrentHashMap<Address, Connection> cons = new ConcurrentHashMap<Address, Connection>();
	private ConcurrentHashMap<Connection, RecieverService> recievers = new ConcurrentHashMap<Connection, RecieverService>();
	private Observable<Message> messageObservable;
	private Observable<ControlEvent> eventObservable;
	private Observer<Message> applicationMessageObserver;
	private Random random;
	private Lock lock = new ReentrantLock();
	private Address hostadress;
	private Address localhost;
	public static final int MAX_ATTEMPTS = 5;
	public static final int MAX_BACKOFF = 3000;
	
	
	public MessageSender(int port) {
		try {
			this.hostadress = new Address(InetAddress.getLocalHost(), port);
			this.localhost = new Address(InetAddress.getLoopbackAddress(), port);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		random = new Random(System.currentTimeMillis()*29 + this.hashCode()*31);
		server = new Server(port);
		messageObservable =  new Observable<Message>();
		eventObservable = new Observable<ControlEvent>();
		
		this.server.register(new ConcreteObserver<Socket>() {
			@Override
			public void notifyObserver(Socket s) {
				handleConnection(s);
			}
		});
		
		// relay messages to the application layer
		applicationMessageObserver = new Observer<Message>() {
			@Override
			public void notifyObserver(Message e) {
				messageObservable.notifyObservers(e);
			}
		};
	}

	/** Start this MessageSender */
	public void start() {
		server.start();
	}
	
	/** Stop this MessageSender. This also disconnects and removes all underlying connections. */
	public void stop() {
		server.stop();
		for(Entry<Connection, RecieverService> r : recievers.entrySet()) {
			r.getValue().stop();
		}
		for(Entry<Address, Connection> c : cons.entrySet()) {
			c.getValue().disconnect();
		}
		recievers.clear();
		cons.clear();
	}
	
	/** Explicitly disconnect any connection to the given address. */
	public synchronized boolean disconnect(Address addr) {
		if(cons.containsKey(addr)) {
			return removeConnection(cons.get(addr));
		} else {
			return false;
		}
	}
	
	/** Handle incoming connections from the server */
	private void handleConnection(Socket s) {
		Connection con  = new Connection(s);
		Message msg = null;
		try {
			msg = con.recieve();
		} catch (SocketException e1) {
			con.disconnect();	
			return;
		}
		if(msg.has("port")) {
			Address addr = con.getAddress();
			addr.setPort(msg.getInt("port"));
			con.setAddress(addr);
			boolean accept = false;
			if(lock.tryLock()) {
				if(!cons.contains(con.getAddress())) {
					accept = true;
					cons.put(con.getAddress(), con);
					addMessageReciever(con);
				}
				lock.unlock();
			} 
			Message rsp = new Message();
			rsp.setId("con");
			rsp.setKey("accept", accept);
			con.send(rsp);
		} else {
			System.err.println("Unknonw message: " + msg);
		}
	}
	
	/** Get a connection to the target address, if no
	    if no connection exists, a new one is created. */
	private Connection getConnection(Address address) {
		lock.lock();
		if(cons.containsKey(address)) {
			lock.unlock();
			return cons.get(address);
		}
		for(int i=0; i<MAX_ATTEMPTS; i++) {
			if(!cons.containsKey(address)) {
				try {
					Connection con = new Connection(address);
					Message msg = new Message();
					msg.setId("con");
					msg.setKey("port", server.getPort());
					con.send(msg);
					Message rcv = con.recieve();
					if(rcv.getBoolean("accept") == true) {
						cons.put(address, con);
						addMessageReciever(con);
						break;
					} else {
						System.err.println("Connection to " + address + " was denied");
					}	
				} catch (ConnectException e) {
					eventObservable.notifyObservers(new ConnectionRefusedEvent(address));
					lock.unlock();
					return null;
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					lock.unlock();
					Thread.sleep(random.nextInt(MAX_BACKOFF));
					lock.lock();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 		
				System.err.println("Retrying");
			} else {
				break;
			}
		} 
		lock.unlock();
		return cons.get(address);
	}
	
	/** Sends the given message to the destination host specified in the message.
	 * This method creates a connection to the host, or uses
	 * an existing one, in case one already exists.
	 * @return true if the message could be dispatched onto a socket without errors,
	 * othrewise false.*/
	public boolean send(Message m) {
		if(!server.isRunning()) {
			return false;
		} 
		m.setId("app");
		// Handle loopback
		if(m.getDestinationAddress().equals(this.hostadress) ||
		  m.getDestinationAddress().equals(this.localhost)) {
			m.setSourceAddress(hostadress);
			applicationMessageObserver.notifyObserver(m);
			return true;
		}		
		try {
			m.setSourceAddress(InetAddress.getLocalHost().getHostAddress() +":" + server.getPort());
			Connection c = null;
			for(;;) {
				c = getConnection(m.getDestinationAddress());
				if(c == null) {
					return false;// Connection refused
				}
				if(c.isConnected()) {	
					return c.send(m);
				} else { // Closed connection, remove it.
					removeConnection(c);
				}
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/** Add a message reciever to the given connection instance. */
	private RecieverService addMessageReciever(Connection c) {
		RecieverService mr = new RecieverService(c, this);
		mr.register(applicationMessageObserver, "app");
		recievers.put(c,mr);
		return mr;
	}
	
	/** Remove the given connection from all tables and data structures. */
	protected synchronized boolean removeConnection(Connection c) {
		if(cons.contains(c)) {
			System.out.println("Remove connection to " + c.getAddress());
			recievers.get(c).stop();
			recievers.remove(c);
			cons.remove(c.getAddress());
			return true;
		} else {
			return false;
		}
	}
	
	/** Registers a message observer to receive incoming messages. */
	public void registerMessageObserver(Observer<Message> obs) {
		this.messageObservable.register(obs);
	}
	
	/** Registers a control event observer to receive control events from this message sender.
	 * Typical control events are DisconnectEvent and ConenctionRefused event. */
	public void registerControlObserver(Observer<ControlEvent> evt) {
		this.eventObservable.register(evt);
	}
	
	/** Get the connection table for this message sender. */
	public ConcurrentHashMap<Address, Connection> getConnections() {
		return cons;
	}
	
	/** Print all connections from and to this message sender. */
	public void printConnections() {
		System.out.println("Connections for message sender at " + server.getPort() + ":");
		for(Entry<Address, Connection> e : cons.entrySet()) {
			Connection c = e.getValue();
			System.out.println(c.getLocalAddress().getHostAddress() +  ":" + c.getLocalPort() + " - " + 
			c.getInetAddress().getHostAddress() + ":" +c.getPort());
		}
	}
	
	/** Get the host address used by this message sender.
	 *  This is the address used by other hosts to send
	 *  messages to this host. */
	public Address getAddress() {
		return this.hostadress;
	}
}
