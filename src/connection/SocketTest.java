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
	
	public void register(Host host){
		//System.out.println("Added host with ip: " + host.getAddr().toString());
	}
	
	public static void main(String args[]){
		SocketTest s = new SocketTest();
		try {
			Connection c = new Connection(InetAddress.getLocalHost(), 8080, s);
			c.send("HEJ");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
