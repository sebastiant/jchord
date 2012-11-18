package network.events;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import network.Address;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

public class Message extends JSONObject{
		
	public Message() {
		super();
	}
	
	public Message(String json) throws JSONException {
		super(json);
	}
	
	public void setDestinationAddress(InetAddress addr, int port) {
		try {
			this.put("_Dst_address", addr.getHostAddress() + ":" + port);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setDestinationAddress(String addr) {
		try {
			this.put("_Dst_address", addr);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Address getDestinationAddress() {
		Address ret = null;
		try {
			ret = new Address((String)this.get("_Dst_address"));		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	
	public Address getSourceAddress() {
		Address ret = null;
		try {
			ret = new Address((String)this.get("_Src_address"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	public String getId() {
		String ret = null;
		try {
			if(this.has("_Id")) {
				ret = (String) this.get("_Id");
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
			o = this.get(key);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return o;
	}
	
	public void setKey(String key, Object o) {
		try {
			o = this.put(key, o);
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
			this.put("_Src_address", source);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setKey(String key, long value) {
		try {
			this.put(key, value);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setId(String id) {
		try {
			this.put("_Id", id);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
