package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Server extends Observable<Socket> implements ServiceInterface {

	private ServerSocket serverSocket;
	private Service service;
	private int port;

	public Server(int port) {
		this.port = port;
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
		} catch(SocketException e) {
			if(e.getMessage().equals("Socket closed")) {
				// Silent ignore
			}
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public void start() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		service = new Service(this);
		service.start();
	}
	
	public void stop() {
		service.stop();
		service = null;
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public int getPort() {
		return serverSocket.getLocalPort();		
	}
	
	public String toString() {
		return "network.Server#" + serverSocket.getLocalPort();
	}
}
