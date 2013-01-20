package tui;

public class Main {

	public static void main(String args[]) {
		int port = 8000;
		int airity = 2;
		long idSpace = 1024;
		
		if(args.length > 1) {
			port = Integer.parseInt(args[1]);
		}
		if(args.length > 2) {
			airity = Integer.parseInt(args[2]);
		}
		if(args.length > 3) {
			idSpace = Long.parseLong(args[3]);
		}
	
		Console cons = new Console(port, idSpace, airity);
		cons.run();
	}
}
