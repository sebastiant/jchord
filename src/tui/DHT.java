package tui;

import overlay.FingerEntry;
import overlay.Node;
import network.Address;

public class DHT {
	
	private Node node = null;
	private boolean connected = false;
	
	/** Provides a simple interface against the overlay */
	public DHT(Address address, long idSpace, int airity) {
		node = new Node(address, idSpace, airity);	
	}
	
	public void connect(Address address) {
		node.connect(address);
		connected = true;
	}
	
	public void showFigers() {
		FingerEntry fe[]  = node.getFingers();
		for(FingerEntry e: fe) {
			System.out.println(e.getKey() + " -> " + e.getPeerEntry());
		}
	}
	
	public void disconnect() {
		node.shutdown();
		node = null;
	}

	
	public boolean isConnected() {
		return connected;
	}
}
