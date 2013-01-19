package network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

import network.events.ConnectionMessageEvent;
import network.events.ControlEvent;
import network.events.DisconnectEvent;
import network.events.Message;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

public class Connection {
	
	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;
	private Address address;
	private boolean closed = false;
	
	public Connection(Address address) throws IOException {
		Socket socket = new Socket(address.getInetAddress(), address.getPort());
		setup(socket); 
	}
	
	
	public Connection(Socket s) {
		setup(s);
	}
	
	private void setup(Socket s) {
		this.socket = s;
		// Address is changed by the upper layer uppon accept.
		this.address = new Address(s.getInetAddress().getHostAddress() + ":" + s.getPort());  
		try {
			this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			this.out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void send(Message message) {
		try {
			out.write(message.toString() + "\n");
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Message recieve() {
		Message ret = null;
		try {
			String line = in.readLine();
			if(line != null) {
				ret = new Message(new JSONObject(line));	
			} else {
				this.disconnect();
			}
		} catch(java.net.SocketException e) {
			System.out.println("Socket closed");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return ret;
	}
	
	public int getPort() {
		return socket.getPort();
	}
	
	public InetAddress getInetAddress() {
		return socket.getInetAddress();
	}
	
	public Address getAddress() {
		return address;
	}
	
	public InetAddress getLocalAddress() {
		return socket.getLocalAddress();
	}
	
	public int getLocalPort() {
		return socket.getLocalPort();
	}
	
	public void setAddress(Address address) {
		this.address = address;
	}

	public void disconnect() {
		try {
			out.flush();
			socket.close();
			closed = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isConnected() {
		return !closed;
	}
}
