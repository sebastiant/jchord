package connection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Connection implements Runnable{
	
	private Thread thread;
	private ConnectionCallback listener;
	private InetAddress inetAddr;
	private int port;
	private Socket socket;
	private boolean connected;
	
	//Constructor for new outgoing connection
	public Connection(InetAddress inetAddr, ConnectionCallback listener, int port) {
		this.listener = listener;
		this.inetAddr = inetAddr;
		this.port = port;
		try{
			socket=new Socket(inetAddr, port);
		}catch(IOException e){
			System.err.println("ioe");
		}
	}
	
	//Constructor for established connection
	public Connection(Socket socket, ConnectionCallback listener) {
		this.thread = new Thread(this);
		this.socket = socket;
		this.connected = true;
		thread.start();
	}
	
	public void connect(InetAddress ip) {
		if(!connected){
			
		}
	}
	
	public void disconnect() {
		
		connected=false;
	}
	
	public void send(String s) {
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
}


