package network.events;

import network.Address;
import network.Connection;

public class DisconnectEvent implements ControlEvent{
	
	/** This event is sent whenever a socket is unintentionally disconnected,
	 * or if it timed out.
	 * 
	 * In practice, this rarely happens anymore since keepalive is no longer 
	 * implemented in the network layer.
	 *  */

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
