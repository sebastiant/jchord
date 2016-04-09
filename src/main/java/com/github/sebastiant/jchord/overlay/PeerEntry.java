package com.github.sebastiant.jchord.overlay;

import com.github.sebastiant.jchord.network.Address;

public class PeerEntry{
	private long id;
	private Address address;

	public PeerEntry(Address address, long id){
		this.address = address;
		this.id = id;
	}

	public long getId(){
		return id;
	}

	public Address getAddress() {
		return address;
	}
	
	public boolean equals(Object o) {
		boolean ret = false;
		if(o instanceof PeerEntry) {
			PeerEntry p = (PeerEntry)o;
			if(p.id == this.id && p.address.equals(this.address)) {
				return true;
			}
		}
		return ret;
	}
	
	public int hashCode() {
		int hash = 1;
		hash *= 31 + id;
		hash *= 17 + address.hashCode();
		return hash;
	}
	
	public String toString() {
		return "(" +id + ") "; 
	}
}
