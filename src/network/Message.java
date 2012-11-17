package network;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

public class Message {

	private JSONObject json;
		
	public Message() {
		json = new JSONObject();
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
	
	public void setContent(JSONObject json) {
		this.json = json;
	}
	
	public void setKey(String key, Object value) {
		try {
			this.json.put(key, value);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Object getKey(String key) {
		Object ret = null;
		try {
			ret = json.get(key);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	
	public boolean hasKey(String key) {
		return json.has(key);
	}
	
	public JSONObject getContent() {
		return this.json;
	}	
}
