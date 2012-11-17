package testing;

import java.net.InetAddress;
import java.net.UnknownHostException;

import overlay.Node;

import network.Message;

public class NodeTest {
	
	public static void main(String[] args){
		Node n1 = new Node(7979);
		Node n2 = new Node(8080);
		Message msg = new Message();
		try {
			msg.setDestinationAddress(InetAddress.getByName("localhost").getHostAddress() + ":8080");
			msg.setKey("text", "jak er json");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		n1.send(msg);
	}
	
}
