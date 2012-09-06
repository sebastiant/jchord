package overlay;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import connection.Connection;
import connection.ConnectionCallback;
import connection.ConnectionListener;
import connection.Host;
import connection.Message;

public class Node implements ConnectionCallback{
	private int arity;
	private int overlaySize;
	private BigInteger myId;
	private int myPort;
	private ConnectionListener listener;
	private Hashtable<PeerEntry, Connection> connections = new Hashtable<PeerEntry, Connection>();
	private Connection successor;
	private Connection predecessor;
	
	public Node(InetAddress inetAddr, int port, int overlaySize, int arity){
		this.overlaySize=overlaySize;
		this.arity=arity;
		this.myPort = port;
		this.listener = new ConnectionListener(port,this); //The "local server"
		this.myId=IDGenerator.getInstance().getId(inetAddr, port);
		this.successor = null;
		this.predecessor = null;
	}

	public void join(InetAddress inetAddr, int port){
		Connection c = new Connection(inetAddr, port, this);;
		c.send(Protocol.Command.JOIN + Protocol.DELIMETER + myPort);
		BigInteger peerID = IDGenerator.getInstance().getId(inetAddr, port);
		PeerEntry entry = new PeerEntry(peerID, port);
		connections.put(entry, c);
	}
	@Override
	public void register(Host host){
		System.out.println("Got register from Host: " + host.getAddr().getHostAddress());
	}
	@Override
	public void disconnected(Connection connection){
		System.out.println("Connection disconnected!");
		//TODO: Remove host from active connections.
	}
	@Override
	public void receive(Message msg) {
		StringTokenizer tok = new StringTokenizer(msg.getMsg(), Protocol.DELIMETER);
		if(!tok.hasMoreTokens()) {
			return;
		}
		Connection con = msg.getConnection();
		switch(Protocol.Command.valueOf(tok.nextToken().toUpperCase())){
		case JOIN:
			try{
				int peerPort = Integer.parseInt(tok.nextToken());
				int oSize = Integer.parseInt(tok.nextToken());
				if(oSize==overlaySize){
					handleJoin(con, peerPort, oSize);
				}else{
					con.send("overlay size differs, shutting down connection");
					con.disconnect();
				}
			}catch(NoSuchElementException e){
				con.send("Unknown request, shutting down connection");
				con.disconnect();		
			}
			break;
		case WELCOME:
			try{
				String succIp = tok.nextToken();
				int succPort = Integer.parseInt(tok.nextToken());
				handleWelcome(con, succIp, succPort);
			}catch(NoSuchElementException e){
				con.send("Unknown request, shutting down connection");
				con.disconnect();		
			}
			break;
		case SUCC:
			try{
				int port = Integer.parseInt(tok.nextToken());
				handleSuccessorInform(con, port);
			}catch(NoSuchElementException e){
				con.send("Unknown request, shutting down connection");
				con.disconnect();		
			}
			break;
		case PREDREQUEST:
			handlePredecessorRequest(con);
			break;
		case PRED:
			try{
				String predIp = tok.nextToken();
				int predPort = Integer.parseInt(tok.nextToken());
				handlePredecessorInform(con, predIp, predPort);
			}catch(NoSuchElementException e){
				con.send("Unknown request, shutting down connection");
				con.disconnect();		
			}
			break;
		default:
			con.send("Unknown request, shutting down connection");
			con.disconnect();
		}
	
		System.out.println("received: " + msg.getMsg() + " from: " + msg.getAddr().toString() + ":"+ msg.getPort());		
	}
	private void handleJoin(Connection con, int peerPort, int overlaySize){
		BigInteger peerID = IDGenerator.getInstance().getId(con.getAddr(), con.getPort());
		PeerEntry entry = new PeerEntry(peerID, peerPort);
		connections.put(entry, con);
		//TODO: send along ip and portnumber to a suitable successor for the node.
		con.send("WELCOME#123#123"); 
	}
	private void handleWelcome(Connection con, String succIp, int succPort){
		//TODO: handle request and propose successor
	}
	private void handlePredecessorRequest(Connection con){
		//TODO: respond
		con.send("PRED#");
	}
	private void handlePredecessorInform(Connection con, String succIp, int succPort){
		//TODO: handle response
	}
	private void handleSuccessorInform(Connection con, int succPort){
		//TODO: handle inform. Add peer as predecessor accordingly.
	}
	public static void main(String argv[]){
		try {
		Node n = new Node(InetAddress.getLocalHost(),8080, 4, 5);
			//n.join(InetAddress.getLocalHost(), 8080);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
