package testing;

import java.net.InetAddress;

import overlay.FingerEntry;
import overlay.FingerTable;
import overlay.Node;
import overlay.PeerEntry;

import java.lang.Math;

import network.Address;
public class NodeTest {
	
	public static void main(String[] args){
		System.out.println("Running tests!");
		//Test ring logic
		/*
		System.out.print("Testinbetween1: ");
		if(testInBetween1())
			System.out.println("Success!");
		else
			System.err.println("Failed.");
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
		System.out.print("Testinbetween4: ");
		if(testInBetween4())
			System.out.println("Success!");
		else
			System.out.println("Failed.");
		
		System.out.print("Testinbetween5: ");
		if(testInBetween5())
			System.out.println("Success!");
		else
			System.out.println("Failed.");
		
		System.out.print("Testinbetween6: ");
		if(testInBetween6())
			System.out.println("Success!");
		else
			System.out.println("Failed.");
		System.out.print("Testinbetween7: ");
		if(testInBetween6())
			System.out.println("Success!");
		else
			System.out.println("Failed.");
		System.out.print("Testinbetween8: ");
		if(testInBetween6())
			System.out.println("Success!");
		else
			System.out.println("Failed.");

		//Test overlay-creation/destruction
		System.out.print("TestJoin2: ");
		if(testJoin2())
			System.out.println("Success!");
		else
			System.out.println("Failed.");
			
		System.out.print("TestJoin2_disconnect: ");
		if(testJoin2_disconnect())
			System.out.println("Success!");
		else
			System.out.println("Failed.");
		*/
		System.out.print("TestJoin3: ");
		if(testJoin3())
			System.out.println("TestJoin3: Success!");
		else
			System.out.println("TestJoin3: Failed.");
			
		/*
		System.out.print("TestJoin3_disconnect: ");
		if(testJoin3_disconnect())
			System.out.println("TestJoin3_disconnect: Success!");
		else
			System.out.println("TestJoin3_disconnect: Failed.");
			*/
	}

	/*
	 * Make two nodes, try to connect one to the other and check that they have updated their predecessors and successors
	 * 
	 */
	public static boolean testJoin2()
	{
		Node n1 = null,n2 = null;
		try
		{
			int arity=2;
			int idspace=2048;
			int n1_port = 7979;
			int n2_port = 8080;
			n1 = new Node(new Address(InetAddress.getLocalHost(), n1_port), idspace, arity);
			n2 = new Node(new Address(InetAddress.getLocalHost(), n2_port), idspace, arity);
			n1.connect(new Address(InetAddress.getLocalHost(), n2_port));
			Thread.sleep(8000);
			long n1_id = n1.getId();
			long n2_id = n2.getId();
			System.out.println("n1: " + n1_id + "succ: " + n1.getSuccessor().getId());
			System.out.println("n1: " + n1_id + "pred: " + n1.getPredecessor().getId());
			System.out.println("n2: " + n2_id + "succ: " + n2.getSuccessor().getId());
			System.out.println("n2: " + n2_id + "pred: " + n2.getPredecessor().getId());

			if(n2.getPredecessor() == null
					|| n2.getSuccessor() == null
					|| n1.getPredecessor() == null
					|| n1.getSuccessor() == null)
			{
				return false;
			}
			if((n2.getPredecessor().getId() == n1_id) && (n2.getSuccessor().getId() == n1_id)
					&& (n1.getPredecessor().getId() == n2_id) && (n1.getSuccessor().getId() == n2_id))
				return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			n1.shutdown();
			n2.shutdown();
		}
		
		return false;
	}
	
	public static boolean testJoin2_disconnect()
	{
		Node n1 = null,n2 = null;
		try
		{
			int arity=2;
			int idspace=2048;
			int n1_port = 7979;
			int n2_port = 8080;
			n1 = new Node(new Address(InetAddress.getLocalHost(), n1_port), idspace, arity);
			n2 = new Node(new Address(InetAddress.getLocalHost(), n2_port), idspace, arity);
			n1.connect(new Address(InetAddress.getLocalHost(), n2_port));
			Thread.sleep(100);
			long n1_id = n1.getId();
			long n2_id = n2.getId();
			if(n2.getPredecessor() == null
					|| n2.getSuccessor() == null
					|| n1.getPredecessor() == null
					|| n1.getSuccessor() == null)
			{
				return false;
			}
			if(!((n2.getPredecessor().getId() == n1_id) && (n2.getSuccessor().getId() == n1_id)
					&& (n1.getPredecessor().getId() == n2_id) && (n1.getSuccessor().getId() == n2_id)))
				return false;
			n1.shutdown();
			System.out.println("n1 (id: "+ n1 +") shutting down...");
			Thread.sleep(2 * Node.PRED_REQ_INTERVAL);
			if(n2.getPredecessor() == null)
				return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			n1.shutdown();
			n2.shutdown();
		}
		
		return true;
	}
	public static boolean testJoin3()
	{
		Node n1 = null, n2 = null, n3 = null;
		try
		{
			int arity=2;
			int idspace=2048;
			int n1_port = 7171;
			int n2_port = 7272;
			int n3_port = 7373;
			n1 = new Node(new Address(InetAddress.getLocalHost(), n1_port), idspace, arity);
			n2 = new Node(new Address(InetAddress.getLocalHost(), n2_port), idspace, arity);
			n3 = new Node(new Address(InetAddress.getLocalHost(), n3_port), idspace, arity);
			long n1_id = n1.getId();
			long n2_id = n2.getId();
			long n3_id = n3.getId();
			System.out.println("ID: "+ n1_id + " connecting to ID: " + n2_id);
			n1.connect(new Address(InetAddress.getLocalHost(), n2_port));
			Thread.sleep(Node.PRED_REQ_INTERVAL * 3);
			System.out.println("ID: "+ n3_id + " connecting to ID: " + n1_id);
			n3.connect(new Address(InetAddress.getLocalHost(), n1_port));
			Thread.sleep(Node.PRED_REQ_INTERVAL * 8);
			FingerEntry[] fe = n1.getFingers();
			System.out.println("n1 ("+n1.getId()+") finger table\n-----------");
			for(FingerEntry e : fe)
			{
				System.out.println(""+e.getKey()+" -> " +e.getPeerEntry().getId());
			}
			System.out.println("n2 ("+n2.getId()+") finger table\n-----------");
			fe = n2.getFingers();
			for(FingerEntry e : fe)
			{
				System.out.println(""+e.getKey()+" -> " +e.getPeerEntry().getId());
			}
			System.out.println("n3 ("+n3.getId()+") finger table\n-----------");
			fe = n3.getFingers();
			for(FingerEntry e : fe)
			{
				System.out.println(""+e.getKey()+" -> " +e.getPeerEntry().getId());
			}
			if(Node.isBetween(n1_id, n2_id, n3_id)){
				if((n1.getPredecessor().getId() == n2_id)
						&& (n1.getSuccessor().getId() == n3_id)
						&& (n2.getPredecessor().getId() == n3_id)
						&& (n2.getSuccessor().getId() == n1_id)
						&& (n3.getPredecessor().getId() == n1_id)
						&& (n3.getSuccessor().getId() == n2_id))
				{
					return true;
				}
			} else /* Node.isBetween(n1_id, n3_id, n2_id) */
			{
				if((n1.getPredecessor().getId() == n3_id)
						&& (n1.getSuccessor().getId() == n2_id)
						&& (n2.getPredecessor().getId() == n1_id)
						&& (n2.getSuccessor().getId() == n3_id)
						&& (n3.getPredecessor().getId() == n2_id)
						&& (n3.getSuccessor().getId() == n1_id))
				{
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			n1.shutdown();
			n2.shutdown();
			n3.shutdown();
		}
		return false;
	}
	public static boolean testJoin3_disconnect()
	{
		Node n1 = null, n2 = null, n3 = null;
		try
		{
			int arity=2;
			int idspace=2048;
			int n1_port = 7171;
			int n2_port = 7272;
			int n3_port = 7373;
			n1 = new Node(new Address(InetAddress.getLocalHost(), n1_port), idspace, arity);
			n2 = new Node(new Address(InetAddress.getLocalHost(), n2_port), idspace, arity);
			n3 = new Node(new Address(InetAddress.getLocalHost(), n3_port), idspace, arity);
			long n1_id = n1.getId();
			long n2_id = n2.getId();
			long n3_id = n3.getId();
			n1.connect(new Address(InetAddress.getLocalHost(), n2_port));
			Thread.sleep(3000);
			n3.connect(new Address(InetAddress.getLocalHost(), n1_port));
			Thread.sleep(3000);
			if(Node.isBetween(n1_id, n2_id, n3_id)){
				if(!((n1.getPredecessor().getId() == n2_id)
						&& (n1.getSuccessor().getId() == n3_id)
						&& (n2.getPredecessor().getId() == n3_id)
						&& (n2.getSuccessor().getId() == n1_id)
						&& (n3.getPredecessor().getId() == n1_id)
						&& (n3.getSuccessor().getId() == n2_id)))
				{
					return false;
				}
			} else /* Node.isBetween(n1_id, n3_id, n2_id) */
			{
				if(!((n1.getPredecessor().getId() == n3_id)
						&& (n1.getSuccessor().getId() == n2_id)
						&& (n2.getPredecessor().getId() == n1_id)
						&& (n2.getSuccessor().getId() == n3_id)
						&& (n3.getPredecessor().getId() == n2_id)
						&& (n3.getSuccessor().getId() == n1_id)))
				{
					return false;
				}
			}
			
			//Kill node 3
			FingerEntry[] fe = n1.getFingers();
			System.out.println("n1 ("+n1.getId()+") finger table\n-----------");
			for(FingerEntry e : fe)
			{
				System.out.println(""+e.getKey()+" -> " +e.getPeerEntry().getId());
			}
			System.out.println("n2 ("+n2.getId()+") finger table\n-----------");
			fe = n2.getFingers();
			for(FingerEntry e : fe)
			{
				System.out.println(""+e.getKey()+" -> " +e.getPeerEntry().getId());
			}
			System.out.println("n3 ("+n3.getId()+") finger table\n-----------");
			fe = n3.getFingers();
			for(FingerEntry e : fe)
			{
				System.out.println(""+e.getKey()+" -> " +e.getPeerEntry().getId());
			}
			System.out.println("----------------\n KILLING NODE 3 \n ----------------");
			n3.shutdown();
			System.out.println("wait");
			Thread.sleep(Node.PRED_REQ_INTERVAL * 12);
			//Check that node 1 and node 2 has updated the ring accordingly.
			/*
			if((n1.getPredecessor().getId() == n2_id)
					&& (n1.getSuccessor().getId() == n2_id)
					&& (n2.getPredecessor().getId() == n1_id)
					&& (n2.getSuccessor().getId() == n1_id))
			{
				return true;
			}
			*/
			System.out.println("node: " + n1_id + " pred: " + n1.getPredecessor().getId());
			System.out.println("node: " + n1_id + " succ: " + n1.getSuccessor().getId());
			System.out.println("node: " + n2_id + " pred: " + n2.getPredecessor().getId());
			System.out.println("node: " + n2_id + " succ: " + n2.getSuccessor().getId());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			n1.shutdown();
			n2.shutdown();
			n3.shutdown();
		}
		return false;
	}
	public static boolean testInBetween1(){
		long l_1 = 40;
		long l_2 = 4;
		long l_3 = 50;
		return Node.isBetween(l_1, l_2, l_3); 
	}
	public static boolean testInBetween2(){
		long l_1 = 3;
		long l_2 = 8000;
		long l_3 = 9000;
		return !Node.isBetween(l_1, l_2, l_3); 
	}
	public static boolean testInBetween3(){
		long l_1 = 950;
		long l_2 = 951;
		long l_3 = 1;
		return !Node.isBetween(l_1, l_2, l_3); 
	}
	public static boolean testInBetween4(){
		long l_1 = 77;
		long l_2 = 4;
		long l_3 = 90;
		return Node.isBetween(l_1, l_2, l_3); 
	}
	public static boolean testInBetween5(){
		long l_1 = 1000;
		long l_2 = 5;
		long l_3 = 20;
		return !Node.isBetween(l_1, l_2, l_3); 
	}
	public static boolean testInBetween6(){
		long l_1 = 1000;
		long l_2 = 20;
		long l_3 = 5;
		return Node.isBetween(l_1, l_2, l_3); 
	}
	public static boolean testInBetween7(){
		long l_1 = 10;
		long l_2 = 5;
		long l_3 = 10;
		return Node.isBetween(l_1, l_2, l_3); 
	}
	public static boolean testInBetween8(){
		long l_1 = 11;
		long l_2 = 1000;
		long l_3 = 11;
		return Node.isBetween(l_1, l_2, l_3); 
	}
}
