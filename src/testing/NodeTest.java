package testing;

import java.net.InetAddress;
import java.net.UnknownHostException;

import overlay.Node;

import network.Address;
import network.Message;

public class NodeTest {
	
	public static void main(String[] args){
		Node n1 = new Node(7979);
		Node n2 = new Node(8080);
		Message msg = new Message();
		try {
			msg.setDestinationAddress(InetAddress.getByName("localhost").getHostAddress() + ":8080");
			msg.setKey(Node.PROTOCOL_COMMAND, Node.PROTOCOL_JOIN);
			msg.setKey(Node.PROTOCOL_JOIN_ID, 123);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try{
			n1.send(new Address(InetAddress.getLocalHost(), 8080), msg);
		} catch(Exception e){
			System.out.println("exception elo ;D");
		}
	}
	
}
