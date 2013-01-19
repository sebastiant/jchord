package overlay;

public class FingerEntry {
	private int key;
	private PeerEntry pe;
	public FingerEntry(int key, PeerEntry pe)
	{
		this.key = key;
		this.pe = pe;
	}
	public int getKey()
	{
		return key;
	}
	public void setKey(int key)
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
