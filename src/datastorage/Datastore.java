package datastorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import overlay.Node;


public class Datastore {
	
	private Map<Long, Object> store;
	
	public Datastore()
	{
		store = new HashMap<Long, Object>();
	}
	
	public void addEntry(long key, Object obj)
	{
		store.put(key, obj);
	}
	
	public Object getEntry(long key)
	{
		return store.get(key);
	}
	public void removeEntry(long key)
	{
		store.remove(key);
	}
	public Map<Long,Object> getAllEntriesNotBetween(long key_1, long key_2)
	{
		Map<Long,Object> res = new HashMap<Long,Object>();
		for(Map.Entry<Long,Object> e : store.entrySet())
		{
			if(!Node.isBetween(e.getKey(), key_1, key_2))
			{
				res.put(e.getKey(), e.getValue());
				store.remove(e.getKey());
			}
		}
		return res;
	}
	
	public boolean isEmpty()
	{
		return store.isEmpty();
	}
	@Override
	public String toString()
	{
		return store.toString();
	}
}
