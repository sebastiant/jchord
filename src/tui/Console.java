package tui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

import overlay.FingerEntry;
import overlay.Node;

import network.Address;

public class Console {

	private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	private boolean running;
	private DHT dht;

	public Console(int port,  long idSpace, int airity) {
		Address addr = null;
		try {
			addr = new Address(InetAddress.getLocalHost(), port);
			this.dht = new DHT(addr, idSpace, airity);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		running = true;
	}

	public void commandLine() throws IOException {
		System.out.print("# ");
		String line = in.readLine();
		String split[] = line.split("\\s+");
		String cmd = split[0].toUpperCase();
		Command command;
		try {
			command = Command.valueOf(cmd);
		} catch(IllegalArgumentException e){
			System.out.println("Unknown command: " + split[0]);
			return;
		}
		switch(command) {
		case CONNECT: {
			if(split.length < 2) {
				System.out.println("No address given");
				break;
			}
			String input = split[1];
			if(input.indexOf(":") == -1) {
				input += ":8000"; // add default port
			}
			Address addr = new Address(input);
			dht.connect(addr);
			System.out.println("Connect message sent");
			break;
		}
		case DISCONNECT: {
			if(dht.isConnected()) {
				System.out.println("Not connected");
			} else {
				dht.disconnect();
				System.out.println("Disconnected");
			}		
			break;
		} 
		case FINGERS: {
			dht.showFigers();
			break;
		}
		case HELP: {
			System.out.println("Syntax: Command [arguments]");
			for(Command s :Command.values()) {
				s.printHelp();
			}
			break;
		}
		case PUT: {
			if(!dht.isConnected()) {
				System.out.println("Not connected");
				break;
			}
			String input = getQuotes(line);
			if(input != null) {
				long key = dht.put(input);
				System.out.println("key = " + key); 
			} else {
				System.out.println("No data provied, (forgot quotes?)");
			}
			break;
		}	
		case GET: {
			if(!dht.isConnected()) {
				System.out.println("Not connected");
				break;
			}
			if(split.length < 2) {
				System.out.println("No key provieded");
				break;
			}
			try {
				long key = Long.parseLong(split[1]);
				String data = dht.get(key);
				System.out.println("data(" + key + ") = " + data);
			} catch(NumberFormatException e) {
				System.out.println("Invalid key");
			}
			break;
		}
		case RM:
		case REMOVE: {
			if(!dht.isConnected()) {
				System.out.println("Not connected");
				break;
			}
			if(split.length < 2) {
				System.out.println("No key provieded");
				break;
			}
			try {
				long key = Long.parseLong(split[1]);
				dht.remove(key);
				String data = dht.get(key);
				System.out.println("data(" + key+ ") = " + data);
			} catch(NumberFormatException e) {
				System.out.println("Invalid key");
			}
			break;
		}
		case PUTKEY: {
			if(!dht.isConnected()) {
				System.out.println("Not connected");
				break;
			}
			if(split.length < 3) {
				System.out.println("Too few arguments");
				break;
			}
			try {
				long key = Long.parseLong(split[1]);
				String input = getQuotes(line);
				if(input != null) {
					dht.putKey(input, key);
				} else {
					System.out.println("No data provied, (forgot quotes?)");
				}
			} catch(NumberFormatException e) {
				System.out.println("Invalid key");
			}
			break;
		}
		case QUIT: {
			System.out.println("Bye...");
			quit();
			break;
		}
		default: {
			System.out.println("Unhandled command: " + cmd);
		}
		}
	}

	public void quit() {
		//TODO cleanup
		running = false;
	}

	private String getQuotes(String line) {
		String[] result = line.split("\"");
		if(result.length < 2) {
			return null;
		}
		return result[1];
	}
	
	public void run() {
		while(running) {
			try {
				commandLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				quit();
			}
		}
	}

}
