package overlay;

import java.net.InetAddress;

import network.Address;

public class PeerEntry{
	private long id;
	private Address addr;
	public PeerEntry(Address addr, long id){
		this.addr = addr;
		this.id = id;
	}
	public long getId(){
		return id;
	}
	public Address getAddress() {
		return addr;
	}
}
