package overlay;

import java.math.BigInteger;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class IDGenerator {
	public static long getId(InetAddress ip, int port, long overlaySize) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BigInteger hash = null;
		byte[] raw = md.digest((ip.toString() + ":" + Integer.toHexString(port)).getBytes());
		hash = new BigInteger(1, raw);
		return hash.mod(new BigInteger(Long.toString(overlaySize))).longValue();
	}

}
