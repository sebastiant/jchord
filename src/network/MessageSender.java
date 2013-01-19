package network;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import network.events.ConnectionRefusedEvent;
import network.events.ControlEvent;
import network.events.DisconnectEvent;
import network.events.Message;

public class MessageSender {
	
	private Server server;
	private ConcurrentHashMap<Address, Connection> cons = new ConcurrentHashMap<Address, Connection>();
	private ConcurrentHashMap<Connection, RecieverService> recievers = new ConcurrentHashMap<Connection, RecieverService>();
	private ConcurrentHashMap<Connection, KeepAliveService> keepAlives = new ConcurrentHashMap<Connection, KeepAliveService>();
	private Observable<Message> messageObservable;
	private Observable<ControlEvent> eventObservable;
	private Observer<Message> applicationMessageObserver;
	private Observer<DisconnectEvent> disconnectObserver;
	private Random random;
	private Lock lock = new ReentrantLock();
	private Address hostadress;
	private Address localhost;
	public static final int MAX_ATTEMPTS = 5;
	public static final int MAX_BACKOFF = 5000;
	public static final int KEEP_ALIVE_TIMEOUT = 10000;
	
	public MessageSender(int port) {
		try {
			this.hostadress = new Address(InetAddress.getLocalHost(), port);
			this.localhost = new Address(InetAddress.getLoopbackAddress(), port);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		random = new Random(System.currentTimeMillis());
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
		
		disconnectObserver = new Observer<DisconnectEvent>() {
			@Override
			public void notifyObserver(DisconnectEvent e) {
				Connection c = e.getConnection();
				
				// Turn off services
				RecieverService mr = recievers.get(c);
				KeepAliveService kps = keepAlives.get(c);
				mr.stop();
				kps.stop();
				
				// Disconnect
				c.disconnect();
				
				// Clean up tables
				recievers.remove(c);
				keepAlives.remove(c);
				cons.remove(c.getAddress());
				
				// Notify application layer
				System.err.println("Disconnect");
				eventObservable.notifyObservers(e);
			}
		};
	}

	public void start() {
		server.start();
	}
	
	public void stop() {
		server.stop();
	}
	
	private void handleConnection(Socket s) {
		Connection con  = new Connection(s);
		Message msg = con.recieve();
		if(msg.has("port")) {
			Address addr = con.getAddress();
			addr.setPort(msg.getInt("port"));
			con.setAddress(addr);
			boolean accept = false;
			if(lock.tryLock()) {
				if(!cons.contains(con.getAddress())) {
					accept = true;
					cons.put(con.getAddress(), con);
				}
				lock.unlock();
				Message rsp = new Message();
				rsp.setId("con");
				rsp.setKey("accept", accept);
				con.send(rsp);
				addKeepAliveService(addMessageReciever(con));
			}		
		} else {
			System.err.println("Unknonw message: " + msg);
		}
	}
	
	private Connection getConnection(Address address) {
		System.out.println("getConnection " + server.getPort());
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
						System.out.println("accept");
						cons.put(address, con);
						addKeepAliveService(addMessageReciever(con));
						break;
					} else {
						System.err.println("Connection to " + address + " was denied");
					}	
				} catch (ConnectException e) {
					eventObservable.notifyObservers(new ConnectionRefusedEvent(address));
					break;
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
	
	public void send(Message m) {
		m.setId("app");
		// Handle loopback
		if(m.getDestinationAddress().equals(this.hostadress) ||
		  m.getDestinationAddress().equals(this.localhost)) {
			m.setSourceAddress(hostadress);
			applicationMessageObserver.notifyObserver(m);
			return;
		}		
		try {
			m.setSourceAddress(InetAddress.getLocalHost().getHostAddress() +":" + server.getPort());
			Connection c = null;
			for(;;) {
				c = getConnection(m.getDestinationAddress());
				if(c.isConnected()) {
					c.send(m);
					break;
				}
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private RecieverService addMessageReciever(Connection c) {
		RecieverService mr = new RecieverService(c);
		mr.register(applicationMessageObserver, "app");
		recievers.put(c,mr);
		return mr;
	}
	
	private void addKeepAliveService(RecieverService mr) {
		KeepAliveService kps = new KeepAliveService(mr, KEEP_ALIVE_TIMEOUT);
		kps.register(disconnectObserver);
		keepAlives.put(mr.getConnection(), kps);
	}
	
	public void registerMessageObserver(Observer<Message> obs) {
		this.messageObservable.register(obs);
	}
	
	public void registerControlObserver(Observer<ControlEvent> evt) {
		this.eventObservable.register(evt);
	}
	
	public ConcurrentHashMap<Address, Connection> getConnections() {
		return cons;
	}
	
	public void printConnections() {
		System.out.println("Connections for message sender at " + server.getPort() + ":");
		for(Entry<Address, Connection> e : cons.entrySet()) {
			Connection c = e.getValue();
			System.out.println(c.getLocalAddress().getHostAddress() +  ":" + c.getLocalPort() + " - " + 
			c.getInetAddress().getHostAddress() + ":" +c.getPort());
		}
	}
}
