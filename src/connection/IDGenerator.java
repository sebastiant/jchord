package connection;

import java.math.BigInteger;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class IDGenerator {
	
	private static IDGenerator instance = null;
	private MessageDigest md = null;
	
	private IDGenerator() throws NoSuchAlgorithmException {
		md = MessageDigest.getInstance("SHA-256");
	}

	public BigInteger getId(InetAddress ip, int port) {
		BigInteger hash = null;
		byte[] raw = md.digest((ip.toString() + ":" + Integer.toHexString(port)).getBytes());
		hash = new BigInteger(1, raw);
		return hash;
	}
	
	public static IDGenerator getInstance() {
		if(instance == null) {
			try {
				instance = new IDGenerator();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} 
		}
		return instance;
	}
}
