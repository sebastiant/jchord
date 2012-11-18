package overlay;

import java.net.InetAddress;

import network.Address;

public class PeerEntry{
	int id;
	private Address addr;
	public PeerEntry(Address addr, int id){
		this.addr = addr;
		this.id = id;
	}
	public int getId(){
		return id;
	}

}
