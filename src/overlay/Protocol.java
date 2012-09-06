package overlay;

public class Protocol {
	public static final String DELIMETER = "#";
	
	public enum Command {
		JOIN, PING, WELCOME, PREDREQUEST, PRED, SUCC
	}
}
