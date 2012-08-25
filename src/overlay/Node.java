package overlay;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import connection.Connection;
import connection.ConnectionCallback;
import connection.ConnectionListener;
import connection.ConnectionTable;
import connection.IDGenerator;
import connection.Message;

public class Node implements ConnectionCallback{
	private BigInteger myId;
	private ConnectionTable connectionTable;
	private ConnectionListener listener;
	public Node(InetAddress inetAddr, int port){
		this.connectionTable = new ConnectionTable();
		this.listener = new ConnectionListener(port,this, connectionTable);
		myId=IDGenerator.getInstance().getId(inetAddr, port);
	}

	public void join(InetAddress inetAddr, int port){
		Connection c = new Connection(inetAddr, port, this);
		connectionTable.put(c);
		c.send("join#" + myId.toString(16));
		c.send("join");
	}
	public void leave(){
		
	}
	
	@Override
	public void receive(Message msg) {
		System.out.println("received: " + msg.getMsg() + " from: " + msg.getAddr().toString() + ":"+msg.getPort());		
	}
	public static void main(String argv[]){
		try {
		Node n = new Node(InetAddress.getLocalHost(),8080);
			n.join(InetAddress.getLocalHost(), 8080);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
