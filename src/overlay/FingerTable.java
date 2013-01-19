package overlay;

import java.util.ArrayList;

public class FingerTable {
	private FingerEntry ft[];
	private int size;
	private int levelSize;
	
	public FingerTable(int k, int n, PeerEntry self)
	{
		size =  (int) (k * Math.log10(n) / Math.log10(2));
		levelSize = size / k;
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
			int key = (int) self.getId() + offset + (int)Math.pow(2, j);
			if(key > n)
				key -= n;
			ft[i] = new FingerEntry(key, self);
		}
		
	}
	
	public FingerEntry[] getFingerTable()
	{
		return ft;
	}
	
	public void setFingerEntry(int key, PeerEntry value)
	{
		for(FingerEntry f : ft)
		{
			if(f.getKey() == key)
				f.setPeerEntry(value);
		}
	}
	/*
	 * closestPrecedingNode
	 * Returns the, from the finger table, closest preceding node for a specific key.
	 * Optimization is very possible here as all entries are iterated through...
	 */
	public PeerEntry closestPrecedingNode(int key, PeerEntry node)
	{
		PeerEntry last = node;
		for(FingerEntry fe : ft)
		{
			if(Node.isBetween(key,last.getId(),fe.getKey()))
			{
				return last;
			}
			last = fe.getPeerEntry();
		}
		return last;
	}
	
	public static boolean isPowerOfTwo(int num)
	{
		return ((Math.log10(num) / Math.log10(2)) - Math.rint(Math.log10(num) / Math.log10(2))) == 0;
	}
}
