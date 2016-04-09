package com.github.sebastiant.jchord.tui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.github.sebastiant.jchord.network.Address;

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
			String[] input = splitQuotes(line);
			if(input != null && input.length >= 3) {
				long key = dht.put(input[1], input[3]);
				System.out.println("key = " + key); 
			} else {
				System.out.println("Invalid syntax");
			}
			break;
		}	
		case GET: {
			if(split.length < 2) {
				System.out.println("No key provieded");
				break;
			}
			try {
				String[] input = splitQuotes(line);
				if(input == null) {
					System.out.println("Invalid key");
				} else {
					DataEntry data = dht.get(input[1]);
					System.out.println("data(" + data.key + ") = " + data.value);
				}
			} catch(NumberFormatException e) {
				System.out.println("Invalid key");
			}
			break;
		}
		case RM: 
		case REMOVE:
		{
			if(split.length < 2) {
				System.out.println("No key provieded");
				break;
			}
			try {
				String[] input = splitQuotes(line);
				if(input == null) {
					System.out.println("Invalid key");
				} else {
					dht.remove(input[1]);
				}
			} catch(NumberFormatException e) {
				System.out.println("Invalid key");
			}
			break;
		}
		case STORE: {
			dht.showStore();
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
		dht.disconnect();
		running = false;
	}

	private String[] splitQuotes(String line) {
		String[] result = line.split("\"");
		if(result.length < 2) {
			return null;
		}
		return result;
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

	public static void main(String args[]) {
		System.out.println("DHT User Interface");
		int port = 8000;
		int arity = 2;
		long idSpace = (long) Math.pow(2, 62);

		if(args.length >= 1) {
			port = Integer.parseInt(args[0]);
		}
		if(args.length >= 2) {
			arity = Integer.parseInt(args[1]);
		}
		if(args.length >= 3) {
			idSpace = Long.parseLong(args[2]);
		}
		System.out.println("Starting with port: " + port + ", arity: " + arity);
		Console cons = new Console(port, idSpace, arity);
		cons.run();
	}

}
