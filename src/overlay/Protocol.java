package overlay;

public class Protocol {
	public static final String DELIMETER = "#";
	
	public enum Command {
		PING, JOIN, WELCOME, PREDREQUEST, PRED, SUCC
	}
}
