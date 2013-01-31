package tui;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import network.Address;
import overlay.FingerEntry;
import overlay.Node;

public class DHT {
	
	private Node node = null;
	private boolean connected = false;
	
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
		connected = true;
	}
	
	public void disconnect() {
		node.shutdown();
		node = null;
		connected = false;
	}
	
	public long put(String data) {
		long key = hash(data);
		node.putObject(key, data);
		return key;
	}
	
	public void putkey(String data, long key) {
		node.putObject(key, data);
	}
	
	public String get(long key) {
		Object o = node.getObject(key);
		String ret = "null";
		if(o != null) {
			ret = o.toString();
		}
		return ret;
	}
	
	public void remove(long key) {
		node.removeObject(key);
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	
	private long hash(String data) {
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
