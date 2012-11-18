package network.events;

import network.Address;
import network.Connection;

public class DisconnectEvent implements ControlEvent{

	private Address source;
	private Connection con;
	
	public DisconnectEvent(Address source, Connection con) {
		this.source = source;
		this.con = con;
	}
	public Address getSource(){
		return source;
	}
	
	public Connection getConnection() {
		return con;
	}
	
}
