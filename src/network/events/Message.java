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
		
	private JSONObject json;
	
	public Message() {
		this.json = new JSONObject();
	}
	
	public Message(JSONObject json) {
		this.json = json;
	}
	
	public void setDestinationAddress(InetAddress addr, int port) {
		try {
			this.json.put("_Dst_address", addr.getHostAddress() + ":" + port);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setDestinationAddress(String addr) {
		try {
			this.json.put("_Dst_address", addr);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Address getDestinationAddress() {
		Address ret = null;
		try {
			ret = new Address((String)this.json.get("_Dst_address"));		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	
	public Address getSourceAddress() {
		Address ret = null;
		try {
			ret = new Address((String)this.json.get("_Src_address"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	public String getId() {
		String ret = null;
		try {
			if(this.json.has("_Id")) {
				ret = (String) this.json.get("_Id");
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
			o = this.json.put(key, o);
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
			this.json.put("_Src_address", source);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setId(String id) {
		try {
			this.json.put("_Id", id);
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
	
	public String toString() {
		return this.json.toString();
	}
}
