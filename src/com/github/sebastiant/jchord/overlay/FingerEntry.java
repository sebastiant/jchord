package com.github.sebastiant.jchord.overlay;

/*
 * FingerEntry - represents an entry in the Fingertable which stores a key and its associated PeerEntry (successing node)
 */
public class FingerEntry {
	private long key;
	private PeerEntry pe;
	public FingerEntry(long key, PeerEntry pe)
	{
		this.key = key;
		this.pe = pe;
	}
	public long getKey()
	{
		return key;
	}
	public void setKey(long key)
	{
		this.key = key;
	}
	public PeerEntry getPeerEntry()
	{
		return pe;
	}
	public void setPeerEntry(PeerEntry pe)
	{
		this.pe = pe;
	}
}
