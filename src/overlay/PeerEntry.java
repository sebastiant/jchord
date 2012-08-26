package overlay;

import java.math.BigInteger;

public class PeerEntry {
	
	private BigInteger id;
	private int peerPort;
	
	public PeerEntry(BigInteger id, int peerPort) {
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
	
	public BigInteger getId() {
		return id;
	}
}
