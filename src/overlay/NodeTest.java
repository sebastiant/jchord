package overlay;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NodeTest {
	
	public static void main(String args[]) {
		try {
			Node a = new Node(InetAddress.getLocalHost(), 9000, 4, 5);
			Node b = new Node(InetAddress.getLocalHost(), 9001, 4, 5);
			a.join(InetAddress.getLocalHost(), 9001);
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}		
	}
	
}
