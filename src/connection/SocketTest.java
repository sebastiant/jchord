package connection;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketTest implements ConnectionCallback {

	private ConnectionListener cl;
	public SocketTest(){
		cl = new ConnectionListener(8089, this);
		try {
			Connection c = new Connection(new Socket("localhost",8089),this);
			c.send("herp?");
			c.send("derpie?!??");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	@Override
	public void receive(Message msg) {
		System.out.println("received: " + msg.getMsg() + "from: " + msg.getAddr().toString() + ":"+msg.getPort());
		
	}
	
	public static void main(String args[]){
		SocketTest s = new SocketTest();
	}

}
