package connection;

import java.math.BigInteger;
import java.util.Hashtable;

public class ConnectionTable {
	
	private Hashtable<BigInteger, Connection> table = new Hashtable<BigInteger, Connection>();
	
	public ConnectionTable() {}
	
	public void put(Connection c) {
		IDGenerator gen = IDGenerator.getInstance();
		BigInteger id = gen.getId(c.getIp(), c.port());
		table.put(id, c);
	}
	
	public Connection get(BigInteger peerId) {
		return table.get(peerId);
	}
}
