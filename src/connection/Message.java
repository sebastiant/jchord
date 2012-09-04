package connection;

import java.net.InetAddress;

/*
 * "Glue class"
 */
public class Message {

	private String msg;
	private Connection con;
	
	public Message(String msg, Connection con){
		this.msg=msg;
		this.con = con;
	}
	
	public String getMsg(){
		return msg;
	}
	public Connection getConnection() {
		return con;
	}
	
	public InetAddress getAddr() {
		return con.getAddr();
	}
	
	public int getPort() {
		return con.getPort();
	}
}
