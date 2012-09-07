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
	private long overlaySize;
	private long myId;
	private int myPort;
	private ConnectionListener listener;
	private Hashtable<Connection, PeerEntry> connections = new Hashtable<Connection, PeerEntry>();
	private Connection successor;
	private Connection predecessor;
	
	public Node(InetAddress inetAddr, int port, long overlaySize, int arity){
		this.overlaySize=overlaySize;
		this.arity=arity;
		this.myPort = port;
		this.listener = new ConnectionListener(port,this); //The "local server"
		this.myId=IDGenerator.getInstance().getId(inetAddr, port, overlaySize);
		this.successor = null;
		this.predecessor = null;
	}

	public void join(InetAddress inetAddr, int port){
		Connection con = new Connection(inetAddr, port, this);
		System.out.println("Sending join to: " + inetAddr.getHostAddress().toString());
		sendMessage(con, Protocol.Command.JOIN + Protocol.DELIMETER + myPort + Protocol.DELIMETER + overlaySize);
		long peerID = IDGenerator.getInstance().getId(inetAddr, port, overlaySize);
		PeerEntry entry = new PeerEntry(peerID, con, port);
		connections.put(con,entry);
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
		printIncomingMessage(msg.getContents());
		StringTokenizer tok = new StringTokenizer(msg.getContents(), Protocol.DELIMETER);
		if(!tok.hasMoreTokens()) {
			return;
		}
		Connection con = msg.getConnection();
		switch(Protocol.Command.valueOf(tok.nextToken().toUpperCase())){
		case JOIN:
			try{
				int peerPort = Integer.parseInt(tok.nextToken());
				int oSize = Integer.parseInt(tok.nextToken());
				handleJoin(con, peerPort, oSize);
			}catch(NoSuchElementException e){
				e.printStackTrace();
				//con.send("Unknown request, shutting down connection");
				con.disconnect();
			}
			break;
		case WELCOME:
			try{
				String succIp = tok.nextToken();
				int succPort = Integer.parseInt(tok.nextToken());
				handleWelcome(con, succIp, succPort);
			}catch(NoSuchElementException e){
				e.printStackTrace();
				//con.send("Unknown request, shutting down connection");
				con.disconnect();		
			}
			break;
		case SUCC:
			try{
				int port = Integer.parseInt(tok.nextToken());
				handleSuccessorInform(con, port);
			}catch(NoSuchElementException e){
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
				con.disconnect();		
			}
			break;
		default:
			con.disconnect();
		}		
	}
	private void handleJoin(Connection con, int peerPort, int overlaySize){
		System.out.println("handling join");
		if(overlaySize!=this.overlaySize){
			con.disconnect();
		}
		long peerID = IDGenerator.getInstance().getId(con.getAddr(), con.getPort(), overlaySize);
		PeerEntry entry = new PeerEntry(peerID, con, peerPort);
		connections.put(con, entry);
		
		//Reply with ip and port to a suitable(?) successor for the peer.
		if(successor!=null) {
			PeerEntry succPeer= connections.get(successor);
			
			if(FingerTable.inBetween(myId, succPeer.getId(), peerID)){
				//Refer him to our successor.
				System.out.println("Sending derp");
				sendMessage(con, Protocol.Command.WELCOME + Protocol.DELIMETER + succPeer.getAddr().getHostAddress().toString()
						+ Protocol.DELIMETER + succPeer.getPeerPort());
			}
		}
		else {
			System.out.println("Sending derp");
			//Refer him to ourself. Protocol simplicifacion, we could evolve the protocol to keep this connection instead.
			sendMessage(con, Protocol.Command.WELCOME + Protocol.DELIMETER + con.getAddr().getHostAddress().toString() + Protocol.DELIMETER+listener.getPort());
		}
	}
	private void handleWelcome(Connection con, String succIp, int succPort) {
		//TODO: handle request and propose successor
	}
	private void handlePredecessorRequest(Connection con) {
		//TODO: respond
		sendMessage(con, Protocol.Command.PRED + Protocol.DELIMETER + "...");
	}
	private void handlePredecessorInform(Connection con, String succIp, int succPort) {
		//TODO: handle response
	}
	private void handleSuccessorInform(Connection con, int succPort) {
		//TODO: handle inform. Add peer as predecessor accordingly.
	}
	private void sendMessage(Connection con, String text) {
		try{
			con.send(text);
			System.out.println(listener.getPort() + "@" + InetAddress.getLocalHost().getHostAddress().toString()+ "<< " + text);
		}catch(UnknownHostException e){
				
		}
	}
	private void printIncomingMessage(String text) {
		try{
			System.out.println(listener.getPort() + "@" + InetAddress.getLocalHost().getHostAddress().toString() + ">> " + text);
		}catch(UnknownHostException e){
		}
	}
	public static void main(String argv[]) {
		try {
		Node n = new Node(InetAddress.getLocalHost(),8080, 4, 5);
			//n.join(InetAddress.getLocalHost(), 8080);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
