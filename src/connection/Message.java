package connection;

import java.net.InetAddress;

/*
 * "Glue class"
 */
public class Message {

	private String msg;
	private InetAddress addr;
	private int port;
	
	public Message(String msg, InetAddress addr, int port){
		this.msg=msg;
		this.addr=addr;
		this.port=port;
	}
	
	public String getMsg(){
		return msg;
	}
	public InetAddress getAddr() {
		return addr;
	}
	public int getPort() {
		return port;
	}
}
