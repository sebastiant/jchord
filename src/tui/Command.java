package tui;

public enum Command {
	CONNECT("Join a dht network, connect ADRESS [IDSPACE] [ARITY]"),
	HELP("Print this help"),
	FINGERS("Show fingers"),
	DISCONNECT("Disconnect from ring"),
	QUIT("Exit");
	
	private String description;
	
	private Command(String description) {
		this.description = description;
	}
	
	public String toString() {
		return this.name().toLowerCase();
	}
	
	public void printHelp() {
		System.out.println(this + " - " + description);
	}
}
