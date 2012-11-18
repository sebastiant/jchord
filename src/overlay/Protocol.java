package overlay;

public interface Protocol {
	//Protocol messaging-constants. Lookie lookie, no touchie!
	public static final String PROTOCOL_COMMAND = "comm";
	public static final String PROTOCOL_JOIN = "join";
	public static final String PROTOCOL_JOIN_ID = "joinid";
	public static final String PROTOCOL_JOIN_ARITY = "joinarity";
	public static final String PROTOCOL_JOIN_IDENTIFIERSPACE = "joinidspace";
	public static final String PROTOCOL_DISCONNECT = "disc";
	public static final String PROTOCOL_CLOSEDCONNECTION = "closed";
	public static final String PROTOCOL_DENIED = "denied";
	public static final String PROTOCOL_GRANTED = "granted";
	public static final String PROTOCOL_SUCCESSORINFORM = "succ";
	public static final String PROTOCOL_PREDECESSOR_RESPONSE = "pred";
	public static final String PROTOCOL_PREDECESSOR_REQUEST = "predreq";
	public static final String PROTOCOL_NULL = "null";
	//Node states.
	public static final String STATE_DISCONNECTED = "disconnected";
	public static final String STATE_CONNECTING = "connecting";
	public static final String STATE_CONNECTED = "connected";
	public static final String STATE_CLOSEDCONNECTION = "closed";
	public static final String STATE_PREDECESSOR_REQUEST = "predreq";
}
