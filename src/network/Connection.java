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
	
	private final long KEEPALIVE_FEQ = 5000L;
	private final long KEEPALIVE_TIMEOUT = 10000L;
	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;
	private Service reciever;
	private Service keepAliveSender;
	private Service keepAliveNotifier;
	private Observable<ConnectionMessageEvent> msgObs = new Observable<ConnectionMessageEvent>();
	private Observable<ControlEvent> eventObs = new Observable<ControlEvent>();
	private Address address;
	private long lastPing = System.currentTimeMillis();
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
			reciever = new Service() {
				public void service() {
					Message ret = recieve();
					if(ret != null) {
						if(ret.getId().equals("ping")) {
							lastPing = System.currentTimeMillis();
						} else {
							ConnectionMessageEvent evt = new ConnectionMessageEvent(ret, Connection.this);
							msgObs.notifyObservers(evt);
						}
					}
				}
			};
			keepAliveSender = new Service() {
				@Override
				public void service() {
					Message msg = new Message();
					msg.setId("ping");
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
					//System.out.println("Lastping: " + lastPing);
					if(System.currentTimeMillis() - lastPing > KEEPALIVE_TIMEOUT) { 
						eventObs.notifyObservers(new DisconnectEvent(Connection.this.address, Connection.this));
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
			out.write(message.toString() + "\n");
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Message recieve() {
		Message ret = null;
		try {
			String line = in.readLine();
			if(line != null) {
				ret = new Message(line);	
			} else {
				eventObs.notifyObservers(new DisconnectEvent(address, Connection.this));
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
	
	public void setAddress(Address address) {
		this.address = address;
	}

	public void disconnect() {
		try {
			keepAliveNotifier.stop();
			keepAliveSender.stop();
			reciever.stop();
			out.flush();
			socket.close();
			closed = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void start() {
		if(!closed) {
			reciever.start();
			keepAliveSender.start();
			keepAliveNotifier.start();
		}
	}
	
	public void registerConMsgObserver(Observer<ConnectionMessageEvent> obs) {
		msgObs.register(obs);
	}
	public void registerEventObserver(Observer<ControlEvent> obs) {
		eventObs.register(obs);
	}
}
