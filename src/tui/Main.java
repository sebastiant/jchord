package tui;

public class Main {

	public static void main(String args[]) {
		int port = 9001;
		int airity = 2;
		long idSpace = Long.MAX_VALUE;
		
		if(args.length > 1) {
			port = Integer.parseInt(args[0]);
		}
	
		
		Console cons = new Console(port, idSpace, airity);
		cons.run();
	}
}
