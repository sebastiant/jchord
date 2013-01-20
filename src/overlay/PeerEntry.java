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
	
	public boolean equals(Object o) {
		boolean ret = false;
		if(o instanceof PeerEntry) {
			PeerEntry p = (PeerEntry)o;
			if(p.id == this.id && p.addr.equals(this.addr)) {
				return true;
			}
		}
		return ret;
	}
	
	public int hashCode() {
		int hash = 1;
		hash *= 31 + id;
		hash *= 17 + addr.hashCode();
		return hash;
	}
	
	public String toString() {
		return "(" +id + ") "; 
	}
}
