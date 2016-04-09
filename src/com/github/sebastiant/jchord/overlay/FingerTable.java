package com.github.sebastiant.jchord.overlay;

/*
 * FingerTable
 * Contains a list of FingerEntries that associates a finger to a FingerEntry
 * The size is determined by the arity (k) and the idSpace-size (n)
 */
public class FingerTable {
	private PeerEntry self;
	private FingerEntry ft[];
	private int size;
	private long levelSize;
	
	public FingerTable(int k, long n, PeerEntry self)
	{
		size =  (int) (k * Math.log10(n) / Math.log10(2));
		levelSize = size / k;
		this.self = self;
		if(!isPowerOfTwo(n) || k < 1 || n < size)
		{
			throw new RuntimeException("Can not instantiate FingerTable object with passed parameters.");
		}
		
		System.out.println("Identifier space size: " + n + " (" + (int)(Math.log10(n) / Math.log10(2)) + "^2).");
		System.out.println("Routing table size: " + size);
		System.out.println("Level size: " + levelSize);
		
		ft = new FingerEntry[size];
		for(int offset = 0, j = 0, i = 0; i < size; i++,j++)
		{
			if(j == levelSize)
			{
				offset += n / k;
				j = 0;
			}
			long key = self.getId() + offset + (long)Math.pow(2, j);
			if(key > n)
				key -= n;
			ft[i] = new FingerEntry(key, self);
		}
		
	}
	
	public FingerEntry[] getEntries()
	{
		return ft;
	}
	
	public void setFingerEntry(long key, PeerEntry value)
	{
		for(FingerEntry f : ft)
		{
			if(f.getKey() == key)
				f.setPeerEntry(value);
		}
	}
	public PeerEntry getFingerEntry(long key)
	{
		for(FingerEntry f : ft)
		{
			if(f.getKey() == key)
				return f.getPeerEntry();
		}
		return null;
	}
	/*
	 * Replace all occurences of <failedNode> with <successor>. This is done so that when a node is detected to be
	 * offline, it is removed from the fingertable to not be recepient to any further requests. The successor is always
	 * an option to route messages to, and is therefore used untill the fingerentry is updated.
	 */
	public void repairFingerTable(PeerEntry successor, PeerEntry failedNode)
	{
		for(FingerEntry f : ft)
		{
			if(f.getPeerEntry().getId() == failedNode.getId())
			{
				f.setPeerEntry(successor);
			}
		}
	}
	/*
	 * closestPrecedingNode
	 * Returns the, from the finger table, closest preceding node for a specific key.
	 * Optimization is very possible here as all entries are iterated through...
	 */
	public PeerEntry closestPrecedingNode(long key)
	{
		for(int i = size-1; i >= 0; i--)
		{
			if(ft[i] != null)
			{
				if(Node.isBetween(ft[i].getPeerEntry().getId(), self.getId(), key) && key != ft[i].getPeerEntry().getId())
				{
					return ft[i].getPeerEntry();
				}
			}
		}
		return self;
	}
	
	/*
	 *isPowerOfTwo -returns true if the passed value <num> is a power of two.
	 */
	public static boolean isPowerOfTwo(long num)
	{
		return ((Math.log10(num) / Math.log10(2)) - Math.rint(Math.log10(num) / Math.log10(2))) == 0;
	}
}
