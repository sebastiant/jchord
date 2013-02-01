package testing;

import java.net.InetAddress;
import java.net.UnknownHostException;

import network.Address;

import overlay.Node;

public class BunchOfNodes {

	private static Node[] node = new Node[100];
	public static final int PORT = 9000;
	public static final long IDSPACE = (long) Math.pow(2, 62);
	public static final int ARITY = 2; 
	
	public static void main(String[] args) {
		for(int i=0; i<10; i++) {
			node[i] = new Node(new Address("127.0.0.1:" + (PORT+i)), IDSPACE, ARITY);
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int i=1; i<10; i++) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			node[i].connect(node[0].getAddress());
		}
	}
}
