package com.github.sebastiant.jchord.overlay;
import static org.junit.Assert.*;

import java.net.InetAddress;

import com.github.sebastiant.jchord.network.Address;

import org.junit.*;

public class TestRing {
	private Node node1, node2, node3, node4, node5;
	int node1_port, node2_port, node3_port, node4_port, node5_port;
	private int idSpace, arity;
	
	@Before
	public void setUp()
	{
		node1_port = 4401;
		node2_port = 4402;
		node3_port = 4403;
		node4_port = 4404;
		node5_port = 4405;
		arity = 2;
		idSpace = 2048;
		try{
			node1 = new Node(new Address(InetAddress.getLocalHost(), node1_port), idSpace, arity);
			node2 = new Node(new Address(InetAddress.getLocalHost(), node2_port), idSpace, arity);
			node3 = new Node(new Address(InetAddress.getLocalHost(), node3_port), idSpace, arity);
			node4 = new Node(new Address(InetAddress.getLocalHost(), node4_port), idSpace, arity);
			node5 = new Node(new Address(InetAddress.getLocalHost(), node5_port), idSpace, arity);
		}catch(Exception e)
		{
			System.err.println("Could not initialize nodes!");
		}
	}
	

	/*
	 * Make two nodes, try to connect one to the other and check that they have updated their predecessors and successors
	 * 
	 */
	
	@Test
	public void testRingSize2()
	{
		node1.connect(node2.getAddress());
		try{
			Thread.sleep(2000);
		}catch(Exception e)
		{
			System.err.println("Couldn't sleep!");
		}
		long node1_id = node1.getId();
		long node2_id = node2.getId();
				
		assertTrue((node2.getPredecessor().getId() == node1_id)
				&& (node2.getSuccessor().getId() == node1_id)
				&& (node1.getPredecessor().getId() == node2_id)
				&& (node1.getSuccessor().getId() == node2_id));

	}
	
	@Test
	public void testRingSize2_disconnect()
	{
		node1.connect(node2.getAddress());
		try{
			Thread.sleep(1000);
		} catch(Exception e)
		{
			System.err.println("Couldn't sleep!");
		}
		long node1_id = node1.getId();
		long node2_id = node2.getId();
		assertTrue((node2.getPredecessor().getId() == node1_id)
				&& (node2.getSuccessor().getId() == node1_id)
				&& (node1.getPredecessor().getId() == node2_id)
				&& (node1.getSuccessor().getId() == node2_id));
		node1.shutdown();
		System.out.println("node1 (id: "+ node1_id +") shutting down...");
		try{
			Thread.sleep(2 * Node.PRED_REQ_INTERVAL);
		} catch(Exception e)
		{
			System.err.println("Couldn't sleep!");
		}
		assertTrue(node2.getPredecessor() == null);
	}
	
	@Test
	public void testRingSize3()
	{
		long node1_id = node1.getId();
		long node2_id = node2.getId();
		long node3_id = node3.getId();
		System.out.println("ID: "+ node1_id + " connecting to ID: " + node2_id);
		node1.connect(node2.getAddress());
		try{
			Thread.sleep(Node.PRED_REQ_INTERVAL * 3);
		} catch(Exception e)
		{
			System.err.println("Couldn't sleep!");
		}
		System.out.println("ID: "+ node3_id + " connecting to ID: " + node1_id);
		node3.connect(node1.getAddress());
		try{
			Thread.sleep(Node.PRED_REQ_INTERVAL * 8);
		} catch(Exception e)
		{
			System.err.println("Couldn't sleep!");
		}
		FingerEntry[] fe = node1.getFingers();
		System.out.println("node1 ("+node1.getId()+") finger table\n-----------");
		for(FingerEntry e : fe)
		{
			System.out.println(""+e.getKey()+" -> " +e.getPeerEntry().getId());
		}
		System.out.println("node2 ("+node2.getId()+") finger table\n-----------");
		fe = node2.getFingers();
		for(FingerEntry e : fe)
		{
			System.out.println(""+e.getKey()+" -> " +e.getPeerEntry().getId());
		}
		System.out.println("node3 ("+node3.getId()+") finger table\n-----------");
		fe = node3.getFingers();
		for(FingerEntry e : fe)
		{
			System.out.println(""+e.getKey()+" -> " +e.getPeerEntry().getId());
		}
		if(Node.isBetween(node1_id, node2_id, node3_id)){
			assertTrue((node1.getPredecessor().getId() == node2_id)
					&& (node1.getSuccessor().getId() == node3_id)
					&& (node2.getPredecessor().getId() == node3_id)
					&& (node2.getSuccessor().getId() == node1_id)
					&& (node3.getPredecessor().getId() == node1_id)
					&& (node3.getSuccessor().getId() == node2_id));
		} else /* Node.isBetween(node1_id, node3_id, node2_id) */
		{
			assertTrue((node1.getPredecessor().getId() == node3_id)
					&& (node1.getSuccessor().getId() == node2_id)
					&& (node2.getPredecessor().getId() == node1_id)
					&& (node2.getSuccessor().getId() == node3_id)
					&& (node3.getPredecessor().getId() == node2_id)
					&& (node3.getSuccessor().getId() == node1_id));
		}
	}
	
	@Test
	public void testRingSize3_disconnect()
	{
		long node1_id = node1.getId();
		long node2_id = node2.getId();
		long node3_id = node3.getId();
		node1.connect(node2.getAddress());
		try{
			Thread.sleep(2000);
		} catch(Exception e)
		{
			System.err.println("Couldn't sleep!");
		}
		node3.connect(node1.getAddress());
		try{
			Thread.sleep(2000);
		} catch(Exception e)
		{
			System.err.println("Couldn't sleep!");
		}
		if(Node.isBetween(node1_id, node2_id, node3_id)){
			assertTrue((node1.getPredecessor().getId() == node2_id)
					&& (node1.getSuccessor().getId() == node3_id)
					&& (node2.getPredecessor().getId() == node3_id)
					&& (node2.getSuccessor().getId() == node1_id)
					&& (node3.getPredecessor().getId() == node1_id)
					&& (node3.getSuccessor().getId() == node2_id));
		} else /* Node.isBetween(node1_id, node3_id, node2_id) */
		{
			assertTrue(((node1.getPredecessor().getId() == node3_id)
					&& (node1.getSuccessor().getId() == node2_id)
					&& (node2.getPredecessor().getId() == node1_id)
					&& (node2.getSuccessor().getId() == node3_id)
					&& (node3.getPredecessor().getId() == node2_id)
					&& (node3.getSuccessor().getId() == node1_id)));
		}
		
		FingerEntry[] fe = node1.getFingers();
		System.out.println("node1 ("+node1.getId()+") finger table\n-----------");
		for(FingerEntry e : fe)
		{
			System.out.println(""+e.getKey()+" -> " +e.getPeerEntry().getId());
		}
		System.out.println("node2 ("+node2.getId()+") finger table\n-----------");
		fe = node2.getFingers();
		for(FingerEntry e : fe)
		{
			System.out.println(""+e.getKey()+" -> " +e.getPeerEntry().getId());
		}
		System.out.println("node3 ("+node3.getId()+") finger table\n-----------");
		fe = node3.getFingers();
		for(FingerEntry e : fe)
		{
			System.out.println(""+e.getKey()+" -> " +e.getPeerEntry().getId());
		}
		System.out.println("----------------\n KILLING NODE 3 \n ----------------");
		node3.shutdown();
		try{
			Thread.sleep(Node.PRED_REQ_INTERVAL * 4);
		} catch(Exception e)
		{
			System.err.println("Couldn't sleep!");
		}
		//Check that node 1 and node 2 has updated the ring accordingly.
		System.out.println("node: " + node1_id + " pred: " + node1.getPredecessor().getId());
		System.out.println("node: " + node1_id + " succ: " + node1.getSuccessor().getId());
		System.out.println("node: " + node2_id + " pred: " + node2.getPredecessor().getId());
		System.out.println("node: " + node2_id + " succ: " + node2.getSuccessor().getId());
		
		assertTrue((node1.getPredecessor().getId() == node2_id)
				&& (node1.getSuccessor().getId() == node2_id)
				&& (node2.getPredecessor().getId() == node1_id)
				&& (node2.getSuccessor().getId() == node1_id));
			
	}
	
	@Test
	public void testRingSize4()
	{
		FingerEntry[] fe;
		long node1_id = node1.getId();
		long node2_id = node2.getId();
		long node3_id = node3.getId();
		long node4_id = node4.getId();
		System.out.println("ID: "+ node1_id + " connecting to ID: " + node2_id);
		node1.connect(node2.getAddress());
		try{
			Thread.sleep(Node.PRED_REQ_INTERVAL * 3);
		} catch(Exception e)
		{
			System.err.println("Couldn't sleep!");
		}
		System.out.println("ID: "+ node3_id + " connecting to ID: " + node4_id);
		node3.connect(node4.getAddress());
		try{
			Thread.sleep(Node.PRED_REQ_INTERVAL * 8);
		} catch(Exception e)
		{
			System.err.println("Couldn't sleep!");
		}
		System.out.println("ID: "+ node1_id + " connecting to ID: " + node3_id);
		node1.connect(node3.getAddress());
		try{
			Thread.sleep(Node.PRED_REQ_INTERVAL * 8);
		} catch(Exception e)
		{
			System.err.println("Couldn't sleep!");
		}
		
		fe = node1.getFingers();
		System.out.println("node1 ("+node1.getId()+") finger table\n-----------");
		for(FingerEntry e : fe)
		{
			System.out.println(""+e.getKey()+" -> " +e.getPeerEntry().getId());
		}
		System.out.println("node2 ("+node2.getId()+") finger table\n-----------");
		fe = node2.getFingers();
		for(FingerEntry e : fe)
		{
			System.out.println(""+e.getKey()+" -> " +e.getPeerEntry().getId());
		}
		System.out.println("node3 ("+node3.getId()+") finger table\n-----------");
		fe = node3.getFingers();
		for(FingerEntry e : fe)
		{
			System.out.println(""+e.getKey()+" -> " +e.getPeerEntry().getId());
		}
		System.out.println("node4 ("+node4.getId()+") finger table\n-----------");
		fe = node4.getFingers();
		for(FingerEntry e : fe)
		{
			System.out.println(""+e.getKey()+" -> " +e.getPeerEntry().getId());
		}
		
		if(Node.isBetween(node1_id, node2_id, node4_id) && Node.isBetween(node3_id, node4_id, node2_id)){
			assertTrue((node1.getPredecessor().getId() == node2_id)
					&& (node1.getSuccessor().getId() == node4_id)
					&& (node2.getPredecessor().getId() == node3_id)
					&& (node2.getSuccessor().getId() == node1_id)
					&& (node3.getPredecessor().getId() == node4_id)
					&& (node3.getSuccessor().getId() == node2_id)
					&& (node4.getPredecessor().getId() == node1_id)
					&& (node4.getSuccessor().getId() == node3_id));
		} else if((Node.isBetween(node1_id, node3_id, node4_id) && Node.isBetween(node2_id, node4_id, node3_id)))
		{
			assertTrue((node1.getPredecessor().getId() == node3_id)
					&& (node1.getSuccessor().getId() == node4_id)
					&& (node2.getPredecessor().getId() == node4_id)
					&& (node2.getSuccessor().getId() == node3_id)
					&& (node3.getPredecessor().getId() == node2_id)
					&& (node3.getSuccessor().getId() == node1_id)
					&& (node4.getPredecessor().getId() == node1_id)
					&& (node4.getSuccessor().getId() == node2_id));
		} else if((Node.isBetween(node1_id, node4_id, node3_id) && Node.isBetween(node2_id, node3_id, node4_id)))
		{
			assertTrue((node1.getPredecessor().getId() == node4_id)
					&& (node1.getSuccessor().getId() == node3_id)
					&& (node2.getPredecessor().getId() == node3_id)
					&& (node2.getSuccessor().getId() == node4_id)
					&& (node3.getPredecessor().getId() == node1_id)
					&& (node3.getSuccessor().getId() == node2_id)
					&& (node4.getPredecessor().getId() == node2_id)
					&& (node4.getSuccessor().getId() == node1_id));
		}
		else if((Node.isBetween(node1_id, node2_id, node3_id) && Node.isBetween(node4_id, node3_id, node2_id)))
		{
			assertTrue((node1.getPredecessor().getId() == node2_id)
					&& (node1.getSuccessor().getId() == node3_id)
					&& (node2.getPredecessor().getId() == node4_id)
					&& (node2.getSuccessor().getId() == node1_id)
					&& (node3.getPredecessor().getId() == node1_id)
					&& (node3.getSuccessor().getId() == node2_id)
					&& (node4.getPredecessor().getId() == node2_id)
					&& (node4.getSuccessor().getId() == node1_id));
		}
		else if((Node.isBetween(node1_id, node4_id, node2_id) && Node.isBetween(node3_id, node2_id, node4_id)))
		{
			assertTrue((node1.getPredecessor().getId() == node4_id)
					&& (node1.getSuccessor().getId() == node2_id)
					&& (node2.getPredecessor().getId() == node1_id)
					&& (node2.getSuccessor().getId() == node3_id)
					&& (node3.getPredecessor().getId() == node2_id)
					&& (node3.getSuccessor().getId() == node4_id)
					&& (node4.getPredecessor().getId() == node3_id)
					&& (node4.getSuccessor().getId() == node1_id));
		} else if((Node.isBetween(node1_id, node3_id, node2_id) && Node.isBetween(node4_id, node2_id, node3_id)))
		{
			assertTrue((node1.getPredecessor().getId() == node3_id)
					&& (node1.getSuccessor().getId() == node2_id)
					&& (node2.getPredecessor().getId() == node1_id)
					&& (node2.getSuccessor().getId() == node4_id)
					&& (node3.getPredecessor().getId() == node4_id)
					&& (node3.getSuccessor().getId() == node1_id)
					&& (node4.getPredecessor().getId() == node2_id)
					&& (node4.getSuccessor().getId() == node3_id));
		}
	}
	
	@Test
	public void testRingSize4_disconnect()
	{
		FingerEntry[] fe;
		long node1_id = node1.getId();
		long node2_id = node2.getId();
		long node3_id = node3.getId();
		long node4_id = node4.getId();
		System.out.println("ID: "+ node1_id + " connecting to ID: " + node2_id);
		node1.connect(node2.getAddress());
		try{
			Thread.sleep(Node.PRED_REQ_INTERVAL * 3);
		} catch(Exception e)
		{
			System.err.println("Couldn't sleep!");
		}
		System.out.println("ID: "+ node3_id + " connecting to ID: " + node4_id);
		node3.connect(node4.getAddress());
		try{
			Thread.sleep(Node.PRED_REQ_INTERVAL * 4);
		} catch(Exception e)
		{
			System.err.println("Couldn't sleep!");
		}
		System.out.println("ID: "+ node1_id + " connecting to ID: " + node3_id);
		node1.connect(node3.getAddress());
		try{
			Thread.sleep(Node.PRED_REQ_INTERVAL * 4);
		} catch(Exception e)
		{
			System.err.println("Couldn't sleep!");
		}
		System.out.println("Killing node 4!");
		node4.shutdown();
		try{
			Thread.sleep(Node.PRED_REQ_INTERVAL * 4);
		} catch(Exception e)
		{
			System.err.println("Couldn't sleep!");
		}
		
		fe = node1.getFingers();
		System.out.println("node1 ("+node1.getId()+") finger table\n-----------");
		for(FingerEntry e : fe)
		{
			System.out.println(""+e.getKey()+" -> " +e.getPeerEntry().getId());
		}
		System.out.println("node2 ("+node2.getId()+") finger table\n-----------");
		fe = node2.getFingers();
		for(FingerEntry e : fe)
		{
			System.out.println(""+e.getKey()+" -> " +e.getPeerEntry().getId());
		}
		System.out.println("node3 ("+node3.getId()+") finger table\n-----------");
		fe = node3.getFingers();
		for(FingerEntry e : fe)
		{
			System.out.println(""+e.getKey()+" -> " +e.getPeerEntry().getId());
		}
		
		if(Node.isBetween(node1_id, node2_id, node3_id)){
			assertTrue((node1.getPredecessor().getId() == node2_id)
					&& (node1.getSuccessor().getId() == node3_id)
					&& (node2.getPredecessor().getId() == node3_id)
					&& (node2.getSuccessor().getId() == node1_id)
					&& (node3.getPredecessor().getId() == node1_id)
					&& (node3.getSuccessor().getId() == node2_id));
		} else /* Node.isBetween(node1_id, node3_id, node2_id) */
		{
			assertTrue((node1.getPredecessor().getId() == node3_id)
					&& (node1.getSuccessor().getId() == node2_id)
					&& (node2.getPredecessor().getId() == node1_id)
					&& (node2.getSuccessor().getId() == node3_id)
					&& (node3.getPredecessor().getId() == node2_id)
					&& (node3.getSuccessor().getId() == node1_id));
		}
	}
	@After
	public void cleanUp()
	{
		node1.shutdown();
		node2.shutdown();
		node3.shutdown();
		node4.shutdown();
		node5.shutdown();
		node1 = node2 = node3 = node4 = node5 = null;
	}
	

}
