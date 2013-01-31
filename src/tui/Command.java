package tui;

public enum Command {
	CONNECT("connect ADRESS[:PORT] [IDSPACE] [ARITY]", "Join a dht network"),
	HELP("help", "Print this help"),
	FINGERS("fingers", "Show fingers"),
	PUT("put \"DATA STRING\"", "Insert data into the ring"),
	PUTKEY("put KEY \"DATA STRING\"", "Insert data, use the provided key"),
	GET("get KEY", "Retrieve data from the ring"),
	RM("rm KEY", "Alias for remove"),
	REMOVE("remove KEY", "Remove data with the given key from the ring"),
	DISCONNECT("disconnect", "Disconnect from ring"),
	QUIT("quit", "Exit");
	
	private String description;
	private String invocation;
	
	private Command(String invocation, String description) {
		this.description = description;
		this.invocation = invocation;
	}
	
	public String toString() {
		return this.name().toLowerCase();
	}
	
	public void printHelp() {
		StringBuffer buff = new StringBuffer();
		buff.append(invocation);
		while(buff.length() < 40) {
			buff.append(' ');
		}
		buff.append(" - ");
		buff.append(description);
		System.out.println(buff.toString());
	}
}
