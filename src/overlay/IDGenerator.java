package overlay;

import java.math.BigInteger;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import network.Address;

public class IDGenerator {
	public static long getId(Address address, long overlaySize) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BigInteger hash = null;
		byte[] raw = md.digest((address.toString()).getBytes());
		hash = new BigInteger(1, raw);
		return hash.mod(new BigInteger(Long.toString(overlaySize))).longValue();
	}
}
