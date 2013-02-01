package network.events;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import network.Address;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

public class Message {
	
	/** This is a data structure for representing messages to be serialized to JSON.
	 *  It wraps a JSONObject, implementing most relevant methods, and handles exceptions.
	 *  
	 *  It also implements some application specific features such as the ability to specify
	 *  a source and destination address.
	 *  
	 *  All keys beginning with an underscore (_) is reserved for internal use.
	 * */
		
	private JSONObject json;
	
	public Message() {
		this.json = new JSONObject();
	}
	
	public Message(JSONObject json) {
		this.json = json;
	}
	
	public void setDestinationAddress(InetAddress addr, int port) {
		try {
			this.json.put("_dest", addr.getHostAddress() + ":" + port);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setDestinationAddress(Address addr) {
		 setDestinationAddress(addr.getInetAddress(), addr.getPort());
	}
	
	public void setDestinationAddress(String addr) {
		try {
			this.json.put("_dest", addr);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Address getDestinationAddress() {
		Address ret = null;
		try {
			ret = new Address((String)this.json.get("_dest"));		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	
	public Address getSourceAddress() {
		Address ret = null;
		try {
			ret = new Address((String)this.json.get("_src"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	public String getId() {
		String ret = null;
		try {
			if(this.json.has("_id")) {
				ret = (String) this.json.get("_id");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	
	public Object getKey(String key) {
		Object o = null;
		try {
			o = this.json.get(key);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return o;
	}
	
	
	public void setKey(String key, long value) {
		try {
			this.json.put(key, value);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setKey(String key, int value) {
		try {
			this.json.put(key, value);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setKey(String key, double value) {
		try {
			this.json.put(key, value);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setKey(String key, Object o) {
		try {
			this.json.put(key, o);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setSourceAddress(Address address) {
		this.setSourceAddress(address.toString());
	}
	
	public void setSourceAddress(String source) {
		try {
			this.json.put("_src", source);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setId(String id) {
		try {
			this.json.put("_id", id);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean has(String key){
		return this.json.has(key);
	}
	
	public String getString(String key) {
		String str = null;
		try {
			 str = this.json.getString(key);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;
	}
	
	public Long getLong(String key) {
		Long l = 0L;
		try {
			 l = this.json.getLong(key);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return l;
	}
	
	public boolean getBoolean(String key) {
		boolean b = false;
		try {
			b = this.json.getBoolean(key);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return b;
	}
	
	public int getInt(String key) {
		int i = -1;
		try {
			i = this.json.getInt(key);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return i;
	}
	
	public String toString() {
		return this.json.toString();
	}
}
