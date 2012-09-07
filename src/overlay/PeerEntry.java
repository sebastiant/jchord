package overlay;

import java.math.BigInteger;

public class PeerEntry {
	
	private Long id;
	private int peerPort;
	
	public PeerEntry(long id, int peerPort) {
		this.id = id;
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
}
