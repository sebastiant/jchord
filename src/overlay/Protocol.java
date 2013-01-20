package overlay;

public interface Protocol {
	//Protocol messaging-constants. Lookie lookie, no touchie!
	public static final String PROTOCOL_COMMAND = "comm";
	
	public static final String PROTOCOL_JOIN = "join";
	public static final String PROTOCOL_JOIN_ACCEPT = "joinaccept";
	public static final String PROTOCOL_JOIN_DENIED = "joindenied";
	public static final String PROTOCOL_JOIN_ID = "joinid";
	public static final String PROTOCOL_JOIN_ARITY = "joinarity";
	public static final String PROTOCOL_JOIN_IDENTIFIERSPACE = "joinidspace";
	
	public static final String PROTOCOL_DISCONNECT = "disc";
	public static final String PROTOCOL_CLOSEDCONNECTION = "closed";
	public static final String PROTOCOL_DENIED = "denied";
	public static final String PROTOCOL_GRANTED = "granted";
	
	public static final String PROTOCOL_SUCCESSORINFORM = "succ";
	
	public static final String PROTOCOL_PREDECESSOR_RESPONSE = "predresponse";
	public static final String PROTOCOL_PREDECESSOR_REQUEST = "predreq";
	public static final String PROTOCOL_PREDECESSOR_ID = "predid";
	public static final String PROTOCOL_PREDECESSOR_ADDRESS = "predaddr";
	public static final String PROTOCOL_SENDER_ID = "sid";
	public static final String PROTOCOL_NULL = "null";
	
	public static final String PROTOCOL_FIND_SUCCESSOR = "findsucc";
	public static final String PROTOCOL_FIND_SUCCESSOR_KEY = "succkey";
	public static final String PROTOCOL_FIND_SUCCESSOR_SENDER_ADDR = "findsuccsenderaddr";
	public static final String PROTOCOL_FIND_SUCCESSOR_RESPONSE = "findsuccresp";
	public static final String PROTOCOL_FIND_SUCCESSOR_RESPONSE_ID = "findsuccrespid";
	public static final String PROTOCOL_FIND_SUCCESSOR_RESPONSE_ADDR = "findsuccrespaddr";
	
	public static final String PROTOCOL_CHECK_PREDECESSOR = "ckpred";
	public static final String PROTOCOL_CHECK_PREDECESSOR_RESPONSE = "ckpresp";
	//Node states.
	public static final String STATE_DISCONNECTED = "disconnected";
	public static final String STATE_CONNECTING = "connecting";
	public static final String STATE_CONNECTED = "connected";
	public static final String STATE_CLOSEDCONNECTION = "closed";
	public static final String STATE_PREDECESSOR_REQUEST = "predreq";
}
