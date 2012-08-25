package connection;

import java.net.InetAddress;


public class Connection {
	
	private ConnectionListener listener;
	
	public Connection(ConnectionListener listener) {
		this.listener = listener;
	}
	
	public void connect(InetAddress ip) {
		
	}
	
	public void disconnect() {
		
	}
	
	public void send(String s) {
		
	}
	
	// TODO: recieve event
}


