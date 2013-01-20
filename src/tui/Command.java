package tui;

public enum Command {
	CONNECT("Join a dht network, connect ADRESS [IDSPACE] [ARITY]"),
	HELP("Print this help"),
	QUIT("Disconnect and exit");
	
	private String description;
	
	private Command(String description) {
		this.description = description;
	}
	
	public void printHelp() {
		System.out.println(this + " - " + description);
	}
}
