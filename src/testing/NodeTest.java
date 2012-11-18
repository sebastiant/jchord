package testing;

import java.net.InetAddress;
import overlay.Node;

import network.Address;
import network.events.Message;

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
		boolean result = false;
		try
		{
			int arity=10;
			int idspace=1024;
			int n1_port = 7979;
			int n2_port = 8080;
			Node n1 = new Node(new Address(InetAddress.getLocalHost(), n1_port), idspace, arity);
			Node n2 = new Node(new Address(InetAddress.getLocalHost(), n2_port), idspace, arity);
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
