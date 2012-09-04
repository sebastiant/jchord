package overlay;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.StringTokenizer;

import connection.Connection;
import connection.ConnectionCallback;
import connection.ConnectionListener;
import connection.Host;
import connection.Message;

public class Node implements ConnectionCallback{
	private BigInteger myId;
	private int myPort;
	private ConnectionListener listener;
	private Hashtable<PeerEntry, Connection> connections = new Hashtable<PeerEntry, Connection>();
	
	public Node(InetAddress inetAddr, int port){
		this.myPort = port;
		this.listener = new ConnectionListener(port,this);
		myId=IDGenerator.getInstance().getId(inetAddr, port);
	}

	public void join(InetAddress inetAddr, int port){
		Connection c = new Connection(inetAddr, port, this);;
		c.send(Protocol.Command.JOIN + Protocol.DELIMETER + myPort);
		BigInteger peerID = IDGenerator.getInstance().getId(inetAddr, port);
		PeerEntry entry = new PeerEntry(peerID, port);
		connections.put(entry, c);
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
			Connection c = msg.getConnection();
			int peerPort = Integer.parseInt(tok.nextToken());
			BigInteger peerID = IDGenerator.getInstance().getId(c.getAddr(), c.getPort());
			PeerEntry entry = new PeerEntry(peerID, peerPort);
			connections.put(entry, c);
			System.out.println("join");
		}
	
		System.out.println("received: " + msg.getMsg() + " from: " + msg.getAddr().toString() + ":"+ msg.getPort());		
	}
	@Override
	public void register(Host host){
		System.out.println("Got register from Host: " + host.getAddr().getHostAddress());
	}
	@Override
	public void disconnected(){
		System.out.println("Connection disconnected!");
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
