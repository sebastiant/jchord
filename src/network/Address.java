package network;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Address {
	private InetAddress ip;
	private int port;
	
	public Address(String address) {
		String[] split = address.split(":");
		try {
			this.ip =  InetAddress.getByName(split[0]);
			this.port = Integer.valueOf(split[1]);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
 	
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
		return ip.getHostAddress() + ":" + port;
	}
	
	public InetAddress getInetAddress() {
		return ip;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
}
