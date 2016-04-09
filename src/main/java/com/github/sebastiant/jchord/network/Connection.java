package com.github.sebastiant.jchord.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import com.github.sebastiant.jchord.network.events.Message;

import org.json.JSONException;
import org.json.JSONObject;

/** This class encapsulates a tcp connection. */

public class Connection {
	
	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;
	private Address address;
	private boolean closed = false;
	
	public Connection(Address address) throws IOException {
		Socket socket = new Socket(address.getInetAddress(), address.getPort());
		socket.setKeepAlive(true);
		setup(socket); 
	}
	
	public Connection(Socket s) {
		setup(s);
	}
	
	/** Setup readers and writers for this socket. */
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
	
	/** Sends a message instance.
	 *  The message is serialized to a JSON string and sent over the connection.*/
	public boolean send(Message message)  {
		try {
			if(!socket.isClosed()){
				out.write(message.toString() + "\n");
				out.flush();
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			return false;
		}
	}
	
	/** Receive a message object from this connection.
	 *  The message is deserialized from a JSON string and converted back into a object.*/
	public Message recieve() throws SocketException {
		Message ret = null;
		try {
			String line = in.readLine();
			if(line != null) {
				ret = new Message(new JSONObject(line));	
			} else {
				this.disconnect();
			}
		} catch(SocketException e) {
			throw e;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return ret;
	}
	
	/** Get the remote port from the underlying socket. */
	public int getPort() {
		return socket.getPort();
	}
	
	/** Get the InetAddress from the underlying socket. */
	public InetAddress getInetAddress() {
		return socket.getInetAddress();
	}
	
	/** Get the remote address for this connection.
	 * Note that this is always a host address with the official listening port
	 * of that host, even if the underlying socket is actually originated from another port. */
	public Address getAddress() {
		return address;
	}
	
	/** Get the local address from the underlying socket. */
	public InetAddress getLocalAddress() {
		return socket.getLocalAddress();
	}
	
	/** Get the local port from the underlying socket. */
	public int getLocalPort() {
		return socket.getLocalPort();
	}
	
	/** Change the remote address of this connection. */
	protected void setAddress(Address address) {
		this.address = address;
	}

	/** Flush and close the underlying socket */
	public synchronized void disconnect() {
		try {
		out.flush();
		socket.close();
		closed = true;
		} catch (IOException e) {
			//Ignored
		}
	}
	
	/** Checks if this connection is still connected. */
	public synchronized boolean isConnected() {
		return !closed;
	}
}
