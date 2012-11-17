package network;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import network.events.ControlEvent;
import network.events.DisconnectEvent;

public class ConnectionHandler {
	
	private ConcurrentHashMap<Address, Connection> cons = new ConcurrentHashMap<Address, Connection>();
	private Server server;
	private ConcreteObserver<Message> messageObserver;
	private ConcreteObserver<ControlEvent> eventObserver;
	private Observable<Message> messageObservable;
	private Observable<ControlEvent> eventObservable;
	
	
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
		
		messageObserver = new ConcreteObserver<Message>() {
			@Override
			public void notifyObserver(Message m) {
				messageObservable.notifyObservers(m);
			}
		};
		
		eventObserver = new ConcreteObserver<ControlEvent>() {
			@Override
			public void notifyObserver(ControlEvent e) {
				if(e instanceof DisconnectEvent) {
					DisconnectEvent de = (DisconnectEvent)e;
					Address address = de.getSource();
					Connection con = cons.get(address);
					if(con != null) {
						con.disconnect();
					}
					cons.remove(address);
				}
				eventObservable.notifyObservers(e);
			}
		};
	}
	
	private void handleConnection(Socket s) {
		Connection con  = new Connection(s);
		Address remote = con.getRemoteAddress();
		con.registerMessageObserver(messageObserver);
		con.registerEventObserver(eventObserver);
		con.start();
		cons.put(remote, con);
	}
	
	private Connection getConnection(Address address) {
		 if(!cons.containsKey(address)) {
			Connection con = new Connection(address);
			Address remote = con.getRemoteAddress();
			con.registerMessageObserver(messageObserver);
			con.registerEventObserver(eventObserver);
			con.start();
			cons.put(remote, con);
		} 
		return cons.get(address);
	}
	
	public void send(Message m) {
		try {
			m.setSourceAddress( InetAddress.getLocalHost().getHostAddress() + ":" + server.getPort());
			Connection c = getConnection(m.getDestinationAddress());
			c.send(m);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
	}
	
	public void registerMessageObserver(Observer<Message> obs) {
		this.messageObservable.register(obs);
	}
	
	public void registerControlObserver(Observer<ControlEvent> evt) {
		this.eventObservable.register(evt);
	}
}
