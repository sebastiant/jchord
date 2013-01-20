package tui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Console {

	private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	private boolean running;
	private DHT dht;
	
	public Console() {
		System.out.println("DHT User interface");
		running = true;
		this.dht = new DHT();
	}
	
	public void readInput() throws IOException {
		System.out.print("# ");
		String line = in.readLine();
		String split[] = line.split("\\s+");
		String cmd = split[0].toUpperCase();
		switch(Command.valueOf(cmd)) {
		case CONNECT:
			long idSpace = Long.MAX_VALUE;
			int airity = 2;
			System.out.println("Not implemented yet");
			
			if(split.length > 2) {
				idSpace = Long.parseLong(split[2]);
			}
			if(split.length > 3) {
				airity = Integer.parseInt(split[3]);
			}
			System.out.println("Joinig adress: " + split[1] + " idpace: " + idSpace +  " airity: " + airity);
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
			System.out.println("Unknown command: " + cmd);
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
				readInput();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				quit();
			}
		}
	}
	
}
