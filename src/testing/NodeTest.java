package testing;

import java.net.InetAddress;
import overlay.Node;

import network.Address;
import network.events.Message;
import overlay.Node;
public class NodeTest {
	
	public static void main(String[] args){
		System.out.println("Running tests!");
		System.out.print("Testinbetween1: ");
/*
		if(testInBetween1())
			System.out.println("Success!");
		else
			System.out.println("Failed.");
		System.out.print("Testinbetween2: ");

		if(testInBetween2())
			System.out.println("Success!");
		else
			System.out.println("Failed.");
		System.out.print("Testinbetween3: ");
		if(testInBetween3())
			System.out.println("Success!");
		else
			System.out.println("Failed.");
		*/
		System.out.print("TestJoin1: ");
		if(testJoin1())
			System.out.println("TestJoin1, Success!");
		else
			System.out.println("TestJoin1, Failed.");
		System.out.print("TestJoin2: ");
		if(testJoin2())
			System.out.println("TestJoin2, Success!");
		else
			System.out.println("TestJoin2, Failed.");
			
	}
	
	/*
	 * Make two nodes, try to connect one to the other and check that the resul
	 * 
	 */
	public static boolean testJoin1()
	{
		boolean result = false;
		Node n1 = null,n2 = null;
		try
		{
			int arity=10;
			int idspace=1024;
			int n1_port = 7979;
			int n2_port = 8080;
			n1 = new Node(new Address(InetAddress.getLocalHost(), n1_port), idspace, arity);
			n2 = new Node(new Address(InetAddress.getLocalHost(), n2_port), idspace, arity);
			n1.connect(new Address(InetAddress.getLocalHost(), n2_port));
			Thread.sleep(100);
			long n1_id = n1.getId();
			long n2_id = n2.getId();
			if(n2.getPredecessor() != null){
				if((n2.getPredecessor().getId() == n1_id) && (n2.getSuccessor().getId() == n1_id)
						&& (n1.getPredecessor().getId() == n2_id) && (n1.getSuccessor().getId() == n2_id))
					result = true;
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			n1.shutdown();
			n2.shutdown();
		}
		
		return result;
	}
	public static boolean testJoin2()
	{
		boolean result = false;
		try
		{
			int arity=10;
			int idspace=1024;
			int n1_port = 7171;
			int n2_port = 7272;
			int n3_port = 7373;
			Node n1 = new Node(new Address(InetAddress.getLocalHost(), n1_port), idspace, arity);
			Node n2 = new Node(new Address(InetAddress.getLocalHost(), n2_port), idspace, arity);
			Node n3 = new Node(new Address(InetAddress.getLocalHost(), n3_port), idspace, arity);
			long n1_id = n1.getId();
			long n2_id = n2.getId();
			long n3_id = n3.getId();
			n1.connect(new Address(InetAddress.getLocalHost(), n2_port));
			Thread.sleep(100);
			n3.connect(new Address(InetAddress.getLocalHost(), n1_port));
			Thread.sleep(100);
			if(n2.getPredecessor() != null){
				if((n1.getPredecessor().getId() == n3_id) && (n1.getSuccessor().getId() == n3_id)
						&& (n2.getPredecessor().getId() == n1_id) && (n2.getSuccessor().getId() == n1_id)
						&& (n3.getPredecessor().getId() == n1_id) && (n3.getSuccessor().getId() == n1_id))
					result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	public static boolean testInBetween1(){
		long l_1 = 40;
		long l_2 = 4;
		long l_3 = 50;
		return Node.isBetween(l_1, l_3, l_2); 
	}
	public static boolean testInBetween2(){
		long l_1 = 3;
		long l_2 = 8000;
		long l_3 = 9000;
		return Node.isBetween(l_1, l_2, l_3); 
	}
	public static boolean testInBetween3(){
		long l_1 = 950;
		long l_2 = 951;
		long l_3 = 1;
		return Node.isBetween(l_1, l_2, l_3); 
	}
	public static boolean testInBetween4(){
		long l_1 = 77;
		long l_2 = 4;
		long l_3 = 90;
		return Node.isBetween(l_1, l_2, l_3); 
	}
}
