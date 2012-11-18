package network.events;

import network.Connection;
import network.events.Message;

public class ConnectionMessageEvent {
	
	private Message msg;
	private Connection con;
	
	public ConnectionMessageEvent(Message msg, Connection con) {
		this.msg = msg;
		this.con = con;
	}
	
	public Message getMessage() {
		return msg;
	}

	public Connection getConnection() {
		return con;
	}
}
