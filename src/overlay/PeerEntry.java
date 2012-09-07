package overlay;

import java.net.InetAddress;

import connection.Connection;

public class PeerEntry {
	
	private Long id;
	private Connection connection;
	private int peerPort;
	
	public PeerEntry(long id, Connection connection, int peerPort) {
		this.id = id;
		this.connection = connection;
		this.peerPort = peerPort;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof PeerEntry) {
			PeerEntry p = (PeerEntry)o;
			if(p.id.equals(this.id)) {
				return true;
			}
		}
		return false;
	}
	
	public int getPeerPort() {
		return peerPort;
	}
	
	public Long getId() {
		return id;
	}
	
	public InetAddress getAddr(){
		return connection.getAddr();
	}
	
	public Connection getConnection() {
		return connection;
	}
}
