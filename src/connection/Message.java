package connection;

import java.net.InetAddress;

/*
 * "Glue class"
 */
public class Message {

	private String contents;
	private Connection con;
	
	public Message(String contents, Connection con){
		this.contents=contents;
		this.con = con;
	}
	
	public String getContents(){
		return contents;
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
