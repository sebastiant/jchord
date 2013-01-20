package tui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

import network.Address;

public class Console {

	private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	private boolean running;
	private DHT dht = null;
	
	public Console(int port,  long idSpace, int airity) {
		System.out.println("DHT User interface");
		Address addr = null;
		try {
			addr = new Address(InetAddress.getLocalHost(), port);
			dht = new DHT(addr, idSpace, airity);
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
		case CONNECT:
			Address addr = new Address(split[1]);
			dht.connect(addr);
			System.out.println("Connect message sent");
			break;
		case DISCONNECT:
			if(!dht.isConnected()) {
				System.out.println("Not connected");
			} else {
				dht.disconnect();
				System.out.println("Disconnected");
			}		
			break;
		case FINGERS:
			dht.showFigers();
			break;
		case HELP:
			System.out.println("Syntax: Command [arguments]");
			for(Command s :Command.values()) {
				s.printHelp();
			}
			break;
		case QUIT:
			System.out.println("Bye...");
			quit();
			break;
		default:
			System.out.println("Unhandled command: " + cmd);
			break;
		}
	}
	
	public void quit() {
		//TODO cleanup
		running = false;
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
