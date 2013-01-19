package network.events;

import network.Address;

public class ConnectionRefusedEvent implements ControlEvent{
	private Address remoteAddr;
	public ConnectionRefusedEvent(Address remoteAddr){
		this.remoteAddr = remoteAddr;
	}
	
	public Address getSource(){
		return remoteAddr;
	}
}
