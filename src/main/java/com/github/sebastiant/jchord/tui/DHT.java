package com.github.sebastiant.jchord.tui;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map.Entry;

import com.github.sebastiant.jchord.network.Address;
import com.github.sebastiant.jchord.overlay.FingerTableEntry;
import com.github.sebastiant.jchord.overlay.Node;

public class DHT {
	
	private Node node = null;
	
	public DHT(Address addr, long idSpace, int airity) {
		node = new Node(addr, idSpace, airity);	
		System.out.println("Node address: " + node.getAddress());
	}

	public void showFigers() {
		FingerTableEntry fe[]  = node.getFingers();
		for(FingerTableEntry e: fe) {
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
	
	public void showStore() {
		for(Entry<Long, Object> e: node.getDatastore().getEntries()) {
			System.out.println(e.getKey() + ": " + e.getValue().toString());
		}
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
			e.printStackTrace();
		}
		BigInteger hash;
		byte[] raw = md.digest(data.getBytes());
		hash = new BigInteger(1, raw);
		return hash.mod(new BigInteger(Long.toString(node.getIdSpace()))).longValue();
	}
	
}
