package overlay;

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
