package overlay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class NodeTest {
	
	public static void main(String args[]) {
		try {
			Node_old a = new Node_old(InetAddress.getByName("localhost"), 9000, 4, 5);
			Node_old b = new Node_old(InetAddress.getByName("localhost"), 9001, 4, 5);
			Node_old c = new Node_old(InetAddress.getByName("localhost"), 9002, 4, 5);
			a.join(InetAddress.getByName("localhost"), 9001);
			c.join(InetAddress.getByName("localhost"), 9000);
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		       String name = null;
		       try {
		    	   while(true){
		         name = br.readLine();
		         System.out.println("kiddin, i ain't sendin no msg");
		    	   }
		       } catch (IOException e) {
		         System.out.println("Error!");
		         System.exit(1);
		       }
		       
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}		
	}
	
}
