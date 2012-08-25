package connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Connection implements Runnable{
	
	private Thread thread;
	private ConnectionCallback callback;
	private Socket socket;
	private boolean connected;
	private BufferedReader reader;
	private PrintWriter writer;
	
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
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			
			this.thread = new Thread(this);
			this.connected = true;
			thread.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void disconnect() {
		connected=false;
	}
	
	public void send(String s) {
		writer.println(s);
		writer.flush();
	}

	@Override
	public void run() {
		try {
			while(connected) {
				String data = reader.readLine();
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


