package com.github.sebastiant.jchord.network.events;

import com.github.sebastiant.jchord.network.Address;

public class ConnectionRefusedEvent implements ControlEvent {
	
	private Address remoteAddress;
	public ConnectionRefusedEvent(Address remoteAddress){
		this.remoteAddress = remoteAddress;
	}
	
	public Address getSource(){
		return remoteAddress;
	}
}
