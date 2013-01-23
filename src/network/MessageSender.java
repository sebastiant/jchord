package network;

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

import network.events.ConnectionRefusedEvent;
import network.events.ControlEvent;
import network.events.Message;

public class MessageSender {
	
	private Server server;
	private ConcurrentHashMap<Address, Connection> cons = new ConcurrentHashMap<Address, Connection>();
	private ConcurrentHashMap<Connection, RecieverService> recievers = new ConcurrentHashMap<Connection, RecieverService>();
	//private ConcurrentHashMap<Connection, KeepAliveService> keepAlives = new ConcurrentHashMap<Connection, KeepAliveService>();
	private Observable<Message> messageObservable;
	private Observable<ControlEvent> eventObservable;
	private Observer<Message> applicationMessageObserver;
	//private Observer<DisconnectEvent> disconnectObserver;
	private Random random;
	private Lock lock = new ReentrantLock();
	private Address hostadress;
	private Address localhost;
	public static final int MAX_ATTEMPTS = 5;
	public static final int MAX_BACKOFF = 3000;
	public static final int KEEP_ALIVE_TIMEOUT = 10000;
	
	public MessageSender(int port) {
		try {
			this.hostadress = new Address(InetAddress.getLocalHost(), port);
			this.localhost = new Address(InetAddress.getLoopbackAddress(), port);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
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
		
	/*	disconnectObserver = new Observer<DisconnectEvent>() {
			@Override
			public void notifyObserver(DisconnectEvent e) {
				Connection c = e.getConnection();
				
				// Turn off services
				RecieverService mr = recievers.get(c);
			//	KeepAliveService kps = keepAlives.get(c);
				mr.stop();
			//	kps.stop();
				
				// Disconnect
				try {
					c.disconnect();
				} catch (IOException e1) {
					// Don't care
				}
				
				// Clean up tables
				recievers.remove(c);
				//keepAlives.remove(c);
				cons.remove(c.getAddress());
				
				// Notify application layer
				System.err.println("Disconnect");
				eventObservable.notifyObservers(e);
			}
		};*/
	}

	public void start() {
		server.start();
	}
	
	public void stop() {
		server.stop();
	/*	for(Entry<Connection, KeepAliveService>  k : keepAlives.entrySet()) {
			k.getValue().stop();
		} */
		for(Entry<Connection, RecieverService> r : recievers.entrySet()) {
			r.getValue().stop();
		}
		for(Entry<Address, Connection> c : cons.entrySet()) {
			c.getValue().disconnect();
		}
		//keepAlives.clear();
		recievers.clear();
		cons.clear();
	}
	
	/* Explicitly disconnect this address */
	public boolean disconnect(Address addr) {
		if(cons.contains(addr)) {
			Connection c = cons.get(addr);
			RecieverService rcv = recievers.get(c);
			rcv.stop();
			c.disconnect();
			return true;
		} else  {
			return false;
		}
	}
	
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
				//addKeepAliveService(addMessageReciever(con));
			} 
			Message rsp = new Message();
			rsp.setId("con");
			rsp.setKey("accept", accept);
			try {
				con.send(rsp);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.err.println("Unknonw message: " + msg);
		}
	}
	
	private Connection getConnection(Address address) {
		lock.lock();;
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
						//addKeepAliveService(addMessageReciever(con));
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
	
	public boolean send(Message m) {;
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
					try {		
						c.send(m);
						return true;
					} catch (IOException e) {
						System.err.println(e);
						return false;
					}
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
	
	private RecieverService addMessageReciever(Connection c) {
		RecieverService mr = new RecieverService(c);
		mr.register(applicationMessageObserver, "app");
		recievers.put(c,mr);
		return mr;
	}
	
	protected void removeConnection(Connection c) {
		if(cons.contains(c)) {
			System.out.println("Remove connection to " + c.getAddress());
			recievers.get(c).stop();
			recievers.remove(c);
			cons.remove(c.getAddress());
		}
	}
	
	/*private void addKeepAliveService(RecieverService mr) {
		KeepAliveService kps = new KeepAliveService(mr, KEEP_ALIVE_TIMEOUT);
		kps.register(disconnectObserver);
		keepAlives.put(mr.getConnection(), kps);
	}*/
	
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
	
	public Address getAddress() {
		return this.hostadress;
	}
}
