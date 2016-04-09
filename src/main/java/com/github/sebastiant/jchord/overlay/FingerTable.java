package com.github.sebastiant.jchord.overlay;

public class FingerTable {
	private PeerEntry self;
	private FingerTableEntry ft[];
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
		
		ft = new FingerTableEntry[size];
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
			ft[i] = new FingerTableEntry(key, self);
		}
		
	}
	
	public FingerTableEntry[] getEntries()
	{
		return ft;
	}
	
	public void setFingerEntry(long key, PeerEntry value)
	{
		for(FingerTableEntry f : ft)
		{
			if(f.getKey() == key)
				f.setPeerEntry(value);
		}
	}

	public void repairFingerTable(PeerEntry successor, PeerEntry failedNode)
	{
		for(FingerTableEntry f : ft)
		{
			if(f.getPeerEntry().getId() == failedNode.getId())
			{
				f.setPeerEntry(successor);
			}
		}
	}

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

	public static boolean isPowerOfTwo(long num)
	{
		return ((Math.log10(num) / Math.log10(2)) - Math.rint(Math.log10(num) / Math.log10(2))) == 0;
	}
}
