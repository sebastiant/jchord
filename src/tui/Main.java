package tui;

public class Main {

	public static void main(String args[]) {
		System.out.println("DHT User Interface");
		int port = 8000;
		int arity = 2;
		long idSpace = 1024;
		
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
