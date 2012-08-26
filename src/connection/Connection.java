package connection;

//Godmorgon :)

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Connection implements Runnable{
	public static final int SOCKET_TIMEOUT_TIME=5000;
	private Thread keepAliveThread;
	private Thread thread;
	private ConnectionCallback callback;
	private Socket socket;
	private boolean connected;
	private BufferedReader reader;
	private PrintWriter writer;
	private KeepAlive keepAlive;
	
	//Constructor for new outgoing connection
	public Connection(InetAddress inetAddr, int port, ConnectionCallback callback) {
		try{
			Socket socket=new Socket(inetAddr, port);
			start(callback, socket);
		}catch(IOException e){
			System.err.println("ioe");
		}
	}
	
	//Constructor for established connection
	public Connection(Socket socket, ConnectionCallback callback) {
		start(callback, socket);
	}
	
	private void start(ConnectionCallback callback, Socket socket) {
		this.callback = callback;
		this.socket = socket;
		start();
	}
	
	private void start() {
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.keepAlive = new KeepAlive(5000,writer);
			this.keepAliveThread = new Thread(keepAlive);
			this.thread = new Thread(this);
			this.connected = true;
			thread.start();
			keepAliveThread.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void disconnect() {
		System.out.println("Disconnecting");
		connected=false;
		keepAlive.stop();
	}
	
	public void send(String s) {
		writer.println(s);
		writer.flush();
	}

	@Override
	public void run() {
		try {
			String data;
			while(connected) {
				data = reader.readLine();
				if(data == null) {
					disconnect();
					break;
				}
				Message msg = new Message(data, socket.getInetAddress(), socket.getPort());
				callback.receive(msg);
			}
		} catch (IOException e) {
			connected = false;
			e.printStackTrace();
		}
	}
	
	public InetAddress getIp() {
		return socket.getInetAddress();
	}
	
	public int port() {
		return socket.getPort();
	}
}


