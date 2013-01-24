package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server extends Observable<Socket> implements ServiceInterface {

	private ServerSocket serverSocket;
	private Service service;
	private int port;

	public Server(int port) {
		this.port = port;
		service = new Service(this);
	}
	
	public void service() {
		try {
			Socket socket = serverSocket.accept();
			socket.setKeepAlive(true);
		/*	System.out.println(this + 
								": new connection from " +
								socket.getInetAddress().getHostName() +
								":" +
								socket.getPort()); */
			notifyObservers(socket);
		} catch(SocketException e) {
			if(e.getMessage().equals("Socket closed")) {
				//Silent ignore
				stop();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public void start() {
		if(!service.isRunning()) {
			try {
				serverSocket = new ServerSocket(port);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			service.start();
		}
	}
	
	public void stop() {
		if(service.isRunning()) {
			service.stop();
			try {
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public int getPort() {
		return serverSocket.getLocalPort();		
	}
	
	public String toString() {
		return "network.Server#" + serverSocket.getLocalPort();
	}
	
	public boolean isRunning() {
		return service.isRunning();
	}
}
