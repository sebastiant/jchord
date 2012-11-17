package network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import network.events.ControlEvent;
import network.events.DisconnectEvent;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

public class Connection {
	
	private final long KEEPALIVE_FEQ = 5000L;
	private final long KEEPALIVE_TIMEOUT = 10000L;
	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;
	private Service reciever;
	private Service keepAliveSender;
	private Service keepAliveNotifier;
	private Observable<Message> msgObs = new Observable<Message>();
	private Observable<ControlEvent> eventObs = new Observable<ControlEvent>();
	private Address address;
	private long lastPing = 0L;
	
	public Connection(Address address) {
		try {
			Socket socket = new Socket(address.getInetAddress(), address.getPort());
			setup(socket);
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
			this.address = new Address(inet, port);
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
		this.address = new Address(s.getInetAddress(), s.getPort());
		try {
			this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			this.out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			reciever = new Service() {
				public void service() {
					Message ret = recieve();
					if(ret.hasKey("type") && ret.getKey("type").equals("_Ping")) {
						Connection.this.lastPing  = System.currentTimeMillis();
					} else {
						msgObs.notifyObservers(ret);
					}
				}
			};
			keepAliveSender = new Service() {
				@Override
				public void service() {
					Message msg = new Message();
					msg.setKey("type", "_Ping");
					send(msg);
					try {
						Thread.sleep(KEEPALIVE_FEQ);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			keepAliveNotifier = new Service() {
				@Override
				public void service() {
					if(System.currentTimeMillis() - lastPing > KEEPALIVE_TIMEOUT) { 
						eventObs.notifyObservers(new DisconnectEvent(Connection.this.address));
					}
					try {
						Thread.sleep(KEEPALIVE_TIMEOUT);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void send(Message message) {
		try {
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	
	public Address getRemoteAddress() {
		return address;
	}

	public void disconnect() {
		try {
			keepAliveNotifier.stop();
			keepAliveSender.stop();
			reciever.stop();
			out.flush();
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void start() {
		reciever.start();
		//keepAliveSender.start();
		//keepAliveNotifier.start();
	}
	
	public void registerMessageObserver(Observer<Message> obs) {
		msgObs.register(obs);
	}
	public void registerEventObserver(Observer<ControlEvent> obs) {
		eventObs.register(obs);
	}
}
