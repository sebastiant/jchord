package com.github.sebastiant.jchord.overlay;

public class FingerTableEntry {
	private long key;
	private PeerEntry pe;

	public FingerTableEntry(long key, PeerEntry pe) {
		this.key = key;
		this.pe = pe;
	}

	public long getKey() {
		return key;
	}

	public PeerEntry getPeerEntry() {
		return pe;
	}

	public void setPeerEntry(PeerEntry pe) {
		this.pe = pe;
	}
}