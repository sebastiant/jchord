package com.github.sebastiant.jchord.overlay.datastorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.github.sebastiant.jchord.overlay.Node;


public class Datastore {
	
	private Map<Long, Object> store;
	
	public Datastore()
	{
		store = new ConcurrentHashMap<Long, Object>();
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

	@Override
	public String toString()
	{
		return store.toString();
	}
	
	public Set<Entry<Long, Object>> getEntries() {
		return store.entrySet();
	}
}
