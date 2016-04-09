package com.github.sebastiant.jchord.overlay;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.github.sebastiant.jchord.network.Address;

/*
 * IDGenerator - generates an id by hasing the passed address and applying modulo(overlaySize) on the result.
 */
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
