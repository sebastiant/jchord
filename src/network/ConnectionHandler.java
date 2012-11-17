package network;

import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionHandler extends Observable<Message> {
	
	private ConcurrentHashMap<String, Connection> cons = new ConcurrentHashMap<String, Connection>();
	private Observable<Socket> server;
	private ConcreteObserver<Message> connectionObserver;
	
	
	public ConnectionHandler(Observable<Socket> server) {
		this.server = server;
		
		if(server != null) {
			this.server.register(new ConcreteObserver<Socket>() {
				@Override
				public void notifyObserver(Socket s) {
					handleConnection(s);
				}
			});
		}
		
		connectionObserver = new ConcreteObserver<Message>() {
			@Override
			public void notifyObserver(Message m) {
				notifyObservers(m);
			}
		};
	}
	
	private void handleConnection(Socket s) {
		Connection con  = new Connection(s);
		String remote = con.getAddressString();
		con.register(connectionObserver);
		con.start();
		cons.put(remote, con);
	}
	
	private Connection getConnection(String address) {
		 if(!cons.containsKey(address)) {
			Connection con = new Connection(address);
			String remote = con.getAddressString();
			con.register(connectionObserver);
			con.start();
			cons.put(remote, con);
		} 
		return cons.get(address);
	}
	
	public void send(Message m) {
		Connection c = getConnection(m.getDestinationAddress());
		c.send(m);
	}
}
