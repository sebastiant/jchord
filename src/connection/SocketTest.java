package connection;

<<<<<<< HEAD
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
=======
import java.net.InetAddress;
>>>>>>> bf4bc5ac3c08ba0913d4b8b968ac6fd9c96d80ab
import java.net.UnknownHostException;

public class SocketTest implements ConnectionCallback {

	private ConnectionListener cl;
	private ConnectionTable table = new ConnectionTable();
	
	public SocketTest(){
<<<<<<< HEAD
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
		
=======
		cl = new ConnectionListener(8080, this, table);
>>>>>>> bf4bc5ac3c08ba0913d4b8b968ac6fd9c96d80ab
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
