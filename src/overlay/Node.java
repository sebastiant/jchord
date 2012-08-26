package overlay;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import connection.Connection;
import connection.ConnectionCallback;
import connection.ConnectionListener;
import connection.Host;
import connection.IDGenerator;
import connection.Message;

public class Node implements ConnectionCallback{
	private BigInteger myId;
	private ConnectionListener listener;
	public Node(InetAddress inetAddr, int port){
		this.listener = new ConnectionListener(port,this);
		myId=IDGenerator.getInstance().getId(inetAddr, port);
	}

	public void join(InetAddress inetAddr, int port){
		Connection c = new Connection(inetAddr, port, this);;
		c.send("join#" + myId.toString(16));
		c.send("join");
	}
	public void leave(){
		
	}

	@Override
	public void receive(Message msg) {
		StringTokenizer tok = new StringTokenizer(msg.getMsg(), Protocol.DELIMETER);
		
		if(!tok.hasMoreTokens()) {
			return;
		}
		switch(Protocol.Command.valueOf(tok.nextToken().toUpperCase())){
		case JOIN:
			System.out.println("join");
		}
		
		System.out.println("received: " + msg.getMsg() + " from: " + msg.getAddr().toString() + ":"+msg.getPort());		
	}
	@Override
	public void register(Host host){
		
	}
	public static void main(String argv[]){
		try {
		Node n = new Node(InetAddress.getLocalHost(),8080);
			//n.join(InetAddress.getLocalHost(), 8080);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
