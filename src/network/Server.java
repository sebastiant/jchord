package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Observable<Socket> implements ServiceInterface {

	private ServerSocket serverSocket;
	private Service service;

	public Server(int port) {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void service() {
		try {
			Socket socket = serverSocket.accept();
			System.out.println(this + 
								": new connection from " +
								socket.getInetAddress().getHostName() +
								":" +
								socket.getPort());
			notifyObservers(socket);
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			service.stop();
		}
	}
	
	public void start() {
		service = new Service(this);
		service.start();
	}
	
	public void stop() {
		service.stop();
		service = null;
	}
	
	public int getPort() {
		return serverSocket.getLocalPort();		
	}
	
	public String toString() {
		return "network.Server#" + serverSocket.getLocalPort();
	}
}
