package overlay;

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

	public long getId(InetAddress ip, int port, long overlaySize) {
		BigInteger hash = null;
		byte[] raw = md.digest((ip.toString() + ":" + Integer.toHexString(port)).getBytes());
		hash = new BigInteger(1, raw);
		return hash.mod(new BigInteger(Long.toString(overlaySize))).longValue();
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
