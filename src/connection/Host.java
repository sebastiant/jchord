package connection;

import java.net.InetAddress;

public class Host {
	private InetAddress addr;
	private int port;
	public Host(InetAddress addr, int port){
		this.addr=addr;
		this.port=port;
	}
	public InetAddress getAddr(){
		return addr;
	}
	public int getPort(){
		return port;
	}
}
