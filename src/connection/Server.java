package connection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class Server implements Runnable {
	
	private ServerListener listener;
	private ServerSocket serverSocket;
	
	public Server(ServerListener listener) throws IOException {
		this.listener = listener;
		serverSocket = new ServerSocket();
	}
	
	
	public void send(String s) {
		
	}

	// TODO: recieve event
	
	public void run() {
		
	}
}


