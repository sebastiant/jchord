package com.github.sebastiant.jchord.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server extends Observable<Socket> implements ServiceInterface {

	/** This class wraps a tcp server socket with a thread that once started, 
	 * continuously accepts new incoming connections, until the server is stopped.
	 * Whenever a new connection is made to the Server, all registered observers are notified
	 * with the accepted tcp socket.
	 * */
	
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
		}catch(SocketException e) {
			if(e.getMessage().equals("Socket closed")) {
				stop();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	public synchronized void start() {
		if(!service.isRunning()) {
			try {
				serverSocket = new ServerSocket(port);
			} catch (IOException e) {
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
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public int getPort() {
		return port;	
	}
	
	public String toString() {
		return "Server#" + serverSocket.getLocalPort();
	}

	public synchronized boolean isRunning() {
		return service.isRunning();
	}
}
