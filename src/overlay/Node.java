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
	private PeerEntry successor;
	private PeerEntry predecessor;
	
	public Node(InetAddress inetAddr, int port, long overlaySize, int arity){
		this.overlaySize=overlaySize;
		this.arity=arity;
		this.myPort = port;
		this.listener = new ConnectionListener(port,this); //The "local server"
		this.myId=IDGenerator.getInstance().getId(inetAddr, port, overlaySize);
		this.successor = null;
		this.predecessor = null;
	}
	
	public void join(InetAddress inetAddr, int port) {
		System.out.println("Sending join to: " + inetAddr.getHostAddress().toString());
		Connection con = createConnection(inetAddr, port);
		sendMessage(con, Protocol.Command.JOIN + Protocol.DELIMETER + myPort + Protocol.DELIMETER + overlaySize);
	}
	
	@Override
	public void register(Host host){
		System.out.println("Got register from Host: " + host.getAddr().getHostAddress());
	}
	@Override
	public void disconnected(Connection connection){
		System.out.println("Connection disconnected!");
		connections.remove(connection);
		//TODO: Remove host from active connections.
	}
	@Override
	public void receive(Message msg) {
		printIncomingMessage(msg);
		StringTokenizer tok = new StringTokenizer(msg.getContents(), Protocol.DELIMETER);
		if(!tok.hasMoreTokens()) {
			return;
		}
		Connection con = msg.getConnection();
		switch(Protocol.Command.valueOf(tok.nextToken().toUpperCase())){
		case JOIN:
			System.out.println("JOIN");
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
				try {
					handleWelcome(con, InetAddress.getByName(succIp), succPort);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
			
			if(FingerTable.inBetween(myId, succPeer.getId(), peerID)) {
				//Refer him to our successor.
				System.out.println("Sending derp");
				sendMessage(con, "WELCOME#" + succPeer.getAddr().getHostAddress().toString() + "#"+succPeer.getPeerPort());
			}
		} else {
			System.out.println("Sending derp");
			//Refer him to ourself. Protocol simplicifacion, we could evolve the protocol to keep this connection instead.
			sendMessage(con, "WELCOME#" + con.getAddr().getHostAddress().toString() + "#"+listener.getPort());
		}
	}
	private void handleWelcome(Connection con, InetAddress succIp, int succPort){
		System.out.println("Handle welcome");
		con.disconnect();
		Connection succCon = createConnection(succIp, succPort);
		succCon.send(Protocol.Command.SUCC + Protocol.DELIMETER + myPort);
	}
	private void handlePredecessorRequest(Connection con) {
		System.out.println("Handle pred request");
	}
	private void handlePredecessorInform(Connection con, String succIp, int succPort){
		PeerEntry peer = connections.get(con);
		assert(peer != null);
		if(con.getLocalAddress().getHostAddress().toString().equals(succIp) && myPort == succPort) {
			predecessor = peer;
		} else {
			try {
				handleWelcome(con, InetAddress.getByName(succIp), succPort); // HAXX
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		
		//TODO: handle response
	}
	private void handleSuccessorInform(Connection con, int port) {
		PeerEntry peer = connections.get(con);
		if(peer == null) {
			long peerId = IDGenerator.getInstance().getId(con.getAddr(), port, overlaySize);
			peer = new PeerEntry(peerId, con, port);
			connections.put(con, peer);
		}
		System.out.println("Handle successor infrom.");
		if(predecessor != null) {
			if(!FingerTable.inBetween(predecessor.getId(), myId, peer.getId())) {
				// Keep current predecessor
				con.send(Protocol.Command.PRED + 
				Protocol.DELIMETER + 
				predecessor.getAddr().getHostAddress() + 
				Protocol.DELIMETER +
				predecessor.getPeerPort());
				return;
			} 
		} 
		// Set node as predecessor
		con.send(Protocol.Command.PRED + 
		Protocol.DELIMETER + 
		peer.getAddr().getHostAddress() + 
		Protocol.DELIMETER +
		peer.getPeerPort());
		predecessor = peer;
	}
	private void sendMessage(Connection con, String text){
		con.send(text);
		System.out.println(listener.getPort() + "@" + con.getLocalAddress().getHostAddress().toString() + " << " + text);
	}
	private void printIncomingMessage(Message msg){
		System.out.println(listener.getPort() + "@" + msg.getConnection().getLocalAddress().getHostAddress().toString() + " >> " + msg.getContents());
	}
	
	private Connection createConnection(InetAddress inetAddr, int port) {
		Connection con = new Connection(inetAddr, port, this);
		long peerID = IDGenerator.getInstance().getId(inetAddr, port, overlaySize);
		PeerEntry entry = new PeerEntry(peerID, con, port);
		connections.put(con, entry);
		return con;
	}

	public static void main(String argv[]){
		try {
		Node n = new Node(InetAddress.getLocalHost(), 8080, 4, 5);
			//n.join(InetAddress.getLocalHost(), 8080);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
