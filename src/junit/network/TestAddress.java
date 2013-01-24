package junit.network;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import network.Address;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestAddress {

	@Test
	public void testHashCode() {
		Address a = new Address("192.168.0.1:8081");
		Address b = new Address("192.168.0.1:8081");
		assertTrue(a.hashCode() == b.hashCode());
		assertTrue(a.equals(b) && b.equals(a));
		Address c = new Address("192.168.0.1:1233");
		Address d = new Address("192.168.0.1:9000");
		assertFalse(c.hashCode() == d.hashCode());
		assertFalse(c.equals(d) && d.equals(c));
		Address e = new Address("192.168.0.1:9000");
		Address f = new Address("192.168.0.2:9000");
		assertFalse(e.hashCode() == f.hashCode());
		assertFalse(e.equals(f) && f.equals(e));
		Address g = null;
		Address h = null;
		Address i = null;
		Address j = null;
		try {
			g = new Address(InetAddress.getLocalHost(), 8042);
			h = new Address(InetAddress.getLocalHost(), 8042);
			i = new Address(InetAddress.getLocalHost(), 8043);
			j = new Address(InetAddress.getLoopbackAddress(), 8042);
		} catch (UnknownHostException e1) {
			fail(e1.getMessage());
		}
		assertTrue(g.hashCode() == h.hashCode());
		assertTrue(g.equals(h) && h.equals(g));
		assertFalse(g.hashCode() == i.hashCode());
		assertFalse(g.equals(i) && i.equals(g));
		assertFalse(g.hashCode() == j.hashCode());
		assertFalse(g.equals(j) && j.equals(g));	
	}

	@Test
	public void testAddressString() {
		Address a = new Address("192.168.0.1:9000");
		assertEquals(a.toString(), "192.168.0.1:9000");
		Address b = new Address(InetAddress.getLoopbackAddress(), 9000);
		assertEquals(b.toString(), "127.0.0.1:9000");
	}

	@Test
	public void testEqualsObject() {
		Address a = new Address("192.168.0.1:9000");
		assertTrue(a.equals(new Address("192.168.0.1:9000")));
		assertFalse(a.equals("192.168.0.1:9000"));
	}

	@Test
	public void testGetInetAddress() {
		Address a = new Address("192.168.0.1:9000");
		try {
			assertEquals(a.getInetAddress(), InetAddress.getByName("192.168.0.1"));
		} catch (UnknownHostException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetPort() {
		Address a = new Address("192.168.0.1:9000");
		assertTrue(a.getPort() == 9000);
	}

	@Test
	public void testSetPort() {
		Address a = new Address("192.168.0.1:9000");
		a.setPort(8000);
		assertTrue(a.getPort() == 8000);
		assertEquals(a.toString(), "192.168.0.1:8000");
	}

}
