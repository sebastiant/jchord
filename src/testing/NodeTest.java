package testing;

import java.net.InetAddress;
import java.net.UnknownHostException;

import overlay.Node;

import network.Address;
import network.Message;

public class NodeTest {
	
	public static void main(String[] args){
		System.out.println("Running tests!");
		System.out.print("TestJoin1: ");
		if(testJoin1())
			System.out.println("Success!");
		else
			System.out.println("Failed.");
	}
	
	/*
	 * Make two nodes, try to connect one to the other and check that the resul
	 * 
	 */
	public static boolean testJoin1()
	{
		int arity=10;
		int idspace=1024;
		boolean result = false;
		Node n1 = new Node(7979, 1024, 10);
		Node n2 = new Node(8080, 1024, 10);
		Message msg = new Message();
		try {
			n1.connect(new Address(InetAddress.getLocalHost(), 8080));
			Thread.sleep(100);
			if(n2.getPredecessor() != null){
				if((n2.getPredecessor().getId() == 123) && (n2.getSuccessor().getId() == 123)
						&& (n1.getPredecessor().getId() == 1010) && (n1.getSuccessor().getId() == 1010))
					result = true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
}
