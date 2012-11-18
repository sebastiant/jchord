package network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;

import network.events.ConnectionMessageEvent;
import network.events.ConnectionRefusedEvent;
import network.events.ControlEvent;
import network.events.DisconnectEvent;
import network.events.Message;


/* TODO Handle race conditions when two nodes connect to each other.*/

public class ConnectionHandler {
	
	private ConcurrentHashMap<Address, Connection> cons = new ConcurrentHashMap<Address, Connection>();
	private Server server;
	private ConcreteObserver<ConnectionMessageEvent> messageObserver;
	private ConcreteObserver<ControlEvent> eventObserver;
	private Observable<Message> messageObservable;
	private Observable<ControlEvent> eventObservable;
	private Object putCond = new Object();
	
	public ConnectionHandler(Server server) {
		this.server = server;
		messageObservable =  new Observable<Message>();
		eventObservable = new Observable<ControlEvent>();
		
		if(server != null) {
			this.server.register(new ConcreteObserver<Socket>() {
				@Override
				public void notifyObserver(Socket s) {
					handleConnection(s);
				}
			});
		}
		
		messageObserver = new ConcreteObserver<ConnectionMessageEvent>() {
			@Override
			public void notifyObserver(ConnectionMessageEvent m) {
				Message msg = m.getMessage();
				Connection c = m.getConnection();
				if(msg.getId().equals("control")) {
					if(msg.has("port")) {
						Address addr = new Address(c.getInetAddress().getHostAddress() + ":" + msg.getKey("port"));
						c.setAddress(addr); // Update address
						if(cons.contains(addr)) {
							cons.get(addr).disconnect();
							System.err.println("Connection was already listed, disconnected the old one.. ");
						}
						cons.put(addr, c); // accept connection
						synchronized(putCond) {
							putCond.notifyAll();
						}
						Message response = new Message();
						response.setId("control");
						response.setKey("accept", true);
						c.send(response);
					}
					if(msg.has("accept")) {
						Address addr  = c.getAddress();
						if(cons.contains(addr)) {
							cons.get(addr).disconnect();
							System.err.println("Connection was already listed, disconnected the old one.. ");
						}
						cons.put(addr , c); // Connection was accepted
						synchronized(putCond) {
							putCond.notifyAll();
						}
					} 
				} else {
					messageObservable.notifyObservers(m.getMessage());
				}
			}
		};
		
		eventObserver = new ConcreteObserver<ControlEvent>() {
			@Override
			public void notifyObserver(ControlEvent e) {
				if(e instanceof DisconnectEvent) {
					DisconnectEvent de = (DisconnectEvent)e;
					Connection con =  de.getConnection();
					con.disconnect();
					System.err.println("Dissconencted: " + de.getSource());
					// Don't notify if it was an unaccepted connection
					// that timed out.
					Address src = de.getSource();
					if(cons.contains(src)) {
						cons.remove(src);
						eventObservable.notifyObservers(e);
					}
				}			
			}
		};
	}
	
	private void handleConnection(Socket s) {
		Connection con  = new Connection(s);
		con.registerConMsgObserver(messageObserver);
		con.registerEventObserver(eventObserver);
		con.start();
	}
	
	private Connection getConnection(Address address) {
		if(!cons.containsKey(address)) {
			Connection con;
			try {
				con = new Connection(address);
				con.registerConMsgObserver(messageObserver);
				con.registerEventObserver(eventObserver);
				con.start();
				Message msg = new Message();
				msg.setId("control");
				msg.put("port", server.getPort());
				con.send(msg);
			} catch (java.net.ConnectException e) {
				eventObservable.notifyObservers(new ConnectionRefusedEvent(address));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Connection ret = cons.get(address);
		while(ret == null) {
			try {
				synchronized(putCond) {
					putCond.wait();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ret = cons.get(address);
		}
		return ret;
	}
	
	public void send(Message m) {
		try {
			Connection c = getConnection(m.getDestinationAddress());
			m.setSourceAddress(InetAddress.getLocalHost().getHostAddress() +":" + server.getPort());
			c.send(m);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void registerMessageObserver(Observer<Message> obs) {
		this.messageObservable.register(obs);
	}
	
	public void registerControlObserver(Observer<ControlEvent> evt) {
		this.eventObservable.register(evt);
	}
}
