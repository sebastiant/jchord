package connection;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class SocketTest implements ConnectionCallback {

	private ConnectionListener cl;
	
	public SocketTest(){
		cl = new ConnectionListener(8080, this);
	}
	
	@Override
	public void receive(Message msg) {
		System.out.println("received: " + msg.getMsg() + " from: " + msg.getAddr().toString() + ":"+msg.getPort());
	}
	@Override
	public void register(Host host){
		//System.out.println("Added host with ip: " + host.getAddr().toString());
	}
	@Override
	public void disconnected(){
		System.out.println("Connection channel is disconnected");
	}
	
	public static void main(String args[]){
		SocketTest s = new SocketTest();
		try {
			Connection c = new Connection(InetAddress.getLocalHost(), 8080, s);
			c.send("HEJ");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

}
