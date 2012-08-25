package connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/*
 * Class: ConnectionListener
 * listens for connections and creates Connection:s when accept()ing.
 */
public class ConnectionListener implements Runnable{
	private ServerSocket serverSocket;
	private boolean running;
	private Thread thread;
	private ConnectionCallback cc;
	public ConnectionListener(int port, ConnectionCallback cc){
		try {
			serverSocket = new ServerSocket(port);
			cc = this.cc;
			thread = new Thread(this);
			running = true;
			thread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	@Override
	public void run() {
		Socket s;
		while(running){
			try {
				s=serverSocket.accept();
				Connection c = new Connection(s, cc);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}