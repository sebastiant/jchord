package network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

public class Connection extends Observable<Message>{
	
	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;
	private Service service;

	public Connection(InetAddress address, int port) {
		try {
			setup(new Socket(address, port));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Connection(String address) {
		String[] split = address.split(":");
		try {
			InetAddress inet =  InetAddress.getByName(split[0]);	
			int port = Integer.valueOf(split[1]);
			setup(new Socket(inet, port));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Connection(Socket s) {
		setup(s);
	}
	
	private void setup(Socket s) {
		this.socket = s;
		try {
			this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			this.out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			service = new Service() {
				public void service() {
					notifyObservers(recieve());
				}
			};
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void send(Message message) {
		try {
			message.setSourceAddress(socket.getLocalAddress().getHostAddress() + ":" + socket.getLocalPort());
			out.write(message.getContent().toString() + "\n");
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Message recieve() {
		Message ret = null;
		try {
			ret = new Message(new JSONObject(in.readLine()));
			System.out.println("recieved:" + ret.getContent());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	
	public InetAddress getRemoteAddress() {
		return socket.getInetAddress();
	}
	
	public int getRemotePort() {
		return socket.getPort();
	}
	
	public String getAddressString() {
		return getRemoteAddress().getHostAddress() + ":" + getRemotePort();
	}
	
	public void disconnect() {
		try {
			out.flush();
			socket.close();
			service.stop();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void start() {
		service.start();
	}
}
