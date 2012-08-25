package connection;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class SocketTest implements ConnectionCallback {

	private ConnectionListener cl;
	private ConnectionTable table = new ConnectionTable();
	
	public SocketTest(){
		cl = new ConnectionListener(8080, this, table);
	}
	
	@Override
	public void receive(Message msg) {
		System.out.println("received: " + msg.getMsg() + " from: " + msg.getAddr().toString() + ":"+msg.getPort());
	}
	
	public static void main(String args[]){
		SocketTest s = new SocketTest();
		try {
			Connection c = new Connection(InetAddress.getLocalHost(), 8080, null);
			c.send("HEJ");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
