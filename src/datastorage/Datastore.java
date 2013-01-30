package datastorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
	public List<Object> getAllEntriesToKey(long key)
	{
		return null;
	}
	
	@Override
	public String toString()
	{
		return store.toString();
	}
}