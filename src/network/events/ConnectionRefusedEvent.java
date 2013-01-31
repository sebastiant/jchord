package network.events;

import network.Address;

public class ConnectionRefusedEvent implements ControlEvent{
	
	/** This control event is sent by MessageSender whenever a connection attempt to a
	 * remote host is refused. */
	
	private Address remoteAddr;
	public ConnectionRefusedEvent(Address remoteAddr){
		this.remoteAddr = remoteAddr;
	}
	
	public Address getSource(){
		return remoteAddr;
	}
}
