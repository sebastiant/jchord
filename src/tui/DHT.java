package tui;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import network.Address;
import overlay.FingerEntry;
import overlay.Node;

public class DHT {
	
	private Node node = null;
	
	public DHT(Address addr, long idSpace, int airity) {
		node = new Node(addr, idSpace, airity);	
	}

	public void showFigers() {
		FingerEntry fe[]  = node.getFingers();
		for(FingerEntry e: fe) {
			System.out.println(e.getKey() + " -> " + e.getPeerEntry());
		}
	}
	
	public void connect(Address addr) {
		node.connect(addr);
	}
	
	public void disconnect() {
		node.shutdown();
		node = null;
	}
	
	public long put(String key, String data) {
		long k = hash(key);
		node.putObject(k, data);
		return k;
	}
	
	public DataEntry get(String key) {
		long k = hash(key);
		Object o = node.getObject(k);
		String ret = "null";
		if(o != null) {
			ret = o.toString();
		}
		return new DataEntry(ret, k);
	}
	
	public void remove(String key) {
		node.removeObject(hash(key));
	}
	
	public boolean isConnected() {
		if(node.getState() == node.STATE_CONNECTED) {
			return true;
		} else {
			return false;
		}
	}
	
	
	public long hash(String data) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BigInteger hash = null;
		byte[] raw = md.digest(data.getBytes());
		hash = new BigInteger(1, raw);
		return hash.mod(new BigInteger(Long.toString(node.getIdSpace()))).longValue();
	}
	
}
