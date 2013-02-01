package tui;

public enum Command {
	CONNECT("connect ADDRESS[:PORT] [IDSPACE] [ARITY]", "Join a dht network"),
	HELP("help", "Print this help"),
	FINGERS("fingers", "Show fingers"),
	PUT("put \"KEY STRING\" \"DATA STRING\"", "Insert data into the ring"),
	GET("get \"KEY STRING\"", "Retrieve data from the ring"),
	RM("rm \"KEY STRING\"", "Alias for remove"),
	REMOVE("remove \"KEY STRING\"", "Remove data with the given key from the ring"),
	DISCONNECT("disconnect", "Disconnect from ring"),
	STORE("store", "List the store"),
	QUIT("quit", "Exit");
	
	public static final int INDENT = 40;
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
		while(buff.length() < INDENT) {
			buff.append(' ');
		}
		buff.append(" - ");
		buff.append(description);
		System.out.println(buff.toString());
	}
}
