package network;

import java.net.InetAddress;

public class Address {
	private InetAddress ip;
	private int port;
	
	public Address(InetAddress ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	
	public boolean equals(Object o) {
		boolean ret = false;
		if(o instanceof Address)  {
			Address a = (Address)o;
			if(a.ip.equals(this.ip) &&
			   a.port == this.port) {
				ret = true;
			}
		}
		return ret;
	}
	
	public int hashCode() {
		int hash = 1;
		hash *= 31 + ip.hashCode();
		hash *= 17 + port;
		return hash;
	}
	
	public String toString() {
		return ip.toString() + ":" + port;
	}
}
