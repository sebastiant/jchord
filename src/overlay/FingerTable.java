package overlay;

import java.util.ArrayList;

public class FingerTable {
	private FingerEntry ft[];
	private int size;
	private int levelSize;
	
	public FingerTable(int k, int n, PeerEntry self)
	{
		if(!isPowerOfTwo(n) || k < 1 || (n < (k * Math.log10(n) / Math.log10(2))))
		{
			System.out.println("n must be a power of two and k must be greather than 1.");
			throw new RuntimeException("Can not instantiate FingerTable object with passed parameters.");
		}
		System.out.println("ok identifier space size (" + Math.log10(n) / Math.log10(2) + "^2)");
		size =  (int) (k * Math.log10(n) / Math.log10(2));
		levelSize = size / k;
		System.out.println("Creating routing-table of size: " + size + ". levelSize: " + levelSize);
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
	public void printFt()
	{
		for(FingerEntry e : ft)
		{
			System.out.println(e.getKey() + ": " + e.getPeerEntry().getId());
		}
	}
	public static boolean isPowerOfTwo(int num)
	{
		return ((Math.log10(num) / Math.log10(2)) - Math.rint(Math.log10(num) / Math.log10(2))) == 0;
	}
}
