package tui;

import overlay.FingerEntry;
import overlay.Node;
import network.Address;

public class DHT {
	
	private Node node = null;
	
	/** Provides a simple interface against the overlay */
	public DHT() {
		
	}
	
	public void connect(Address address, long idSpace, int airity) {
		if(node != null) {
			node.shutdown();
		}
		node = new Node(address, idSpace, airity);
	}
	
	public void showFigers() {
		FingerEntry fe[]  = node.getFingers();
		for(FingerEntry e: fe) {
			System.out.println(fe.toString());
		}
	}
	
	public void disconnect() {
		node.shutdown();
		node = null;
	}

	
	public boolean isConnected() {
		if(node != null) 
			return true;
		else 
			return false;
	}
}
