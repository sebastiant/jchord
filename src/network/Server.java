package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

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
			notifyObservers(socket);
		//} catch(SocketTimeoutException e){
			//Exception expected. To regularly check if we still should accept connections.
		}catch(SocketException e) {
			if(e.getMessage().equals("Socket closed")) {
				//Silent ignore
				stop();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public synchronized void start() {
		if(!service.isRunning()) {
			try {
				serverSocket = new ServerSocket(port);
			//	serverSocket.setSoTimeout(1000); //Throw SocketTimeoutException if no connection was accepted within 1sec.
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(serverSocket != null) {
				service.start();
			}
		}
	}
	
	public synchronized void stop() {
		if(service.isRunning()) {
			service.stop();
			try {
				//Closes this socket. Any thread currently blocked in accept() will throw a SocketException.
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public int getPort() {
		return port;	
	}
	
	public String toString() {
		return "network.Server#" + serverSocket.getLocalPort();
	}
	
	public synchronized boolean isRunning() {
		return service.isRunning();
	}
}
