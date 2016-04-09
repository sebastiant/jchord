package com.github.sebastiant.jchord.overlay.datastorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.github.sebastiant.jchord.overlay.Node;

/*
 * A storage-class. Stores a map containing hashed keys and objects.
 */
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
	/* 
	 * Returns all of the maps objects which do _not_ have a key between key_1 and key_2 in the a ring (see Node.isBetween for further information)
	 */
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
	
	public Set<Entry<Long, Object>> getEntries() {
		return store.entrySet();
	}
}
