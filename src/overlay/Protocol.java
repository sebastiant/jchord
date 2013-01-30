package overlay;

public interface Protocol {
	//Protocol messaging-constants. Lookie lookie, no touchie!
	
	//COMMAND SPECIFIER
	public static final String PROTOCOL_COMMAND = "comm";
	
	//JOINING
	public static final String PROTOCOL_JOIN = "join";
	public static final String PROTOCOL_JOIN_ACCEPT = "joinaccept";
	public static final String PROTOCOL_JOIN_DENIED = "joindenied";
	public static final String PROTOCOL_JOIN_ID = "joinid";
	public static final String PROTOCOL_JOIN_ARITY = "joinarity";
	public static final String PROTOCOL_JOIN_IDENTIFIERSPACE = "joinidspace";	
	public static final String PROTOCOL_DENIED = "denied";
	
	//SUCCESSOR INFORM
	public static final String PROTOCOL_SUCCESSORINFORM = "succ";
	
	//PREDECESSOR REQUEST /RESPONSE
	public static final String PROTOCOL_PREDECESSOR_RESPONSE = "predresponse";
	public static final String PROTOCOL_PREDECESSOR_REQUEST = "predreq";
	public static final String PROTOCOL_PREDECESSOR_ID = "predid";
	public static final String PROTOCOL_PREDECESSOR_ADDRESS = "predaddr";
	public static final String PROTOCOL_SENDER_ID = "sid";
	public static final String PROTOCOL_NULL = "null";
	
	//SUCCESSOR LIST
	public static final String PROTOCOL_SUCCESSORLIST_1_ADDR = "succlist1addr";
	public static final String PROTOCOL_SUCCESSORLIST_1_ID = "succlist1id";
	public static final String PROTOCOL_SUCCESSORLIST_2_ADDR = "succlist2addr";
	public static final String PROTOCOL_SUCCESSORLIST_2_ID = "succlist2id";
	public static final String PROTOCOL_SUCCESSORLIST_3_ADDR = "succlist3addr";
	public static final String PROTOCOL_SUCCESSORLIST_3_ID = "succlist3id";
	public static final String PROTOCOL_SUCCESSORLIST_4_ADDR = "succlist4addr";
	public static final String PROTOCOL_SUCCESSORLIST_4_ID = "succlist4id";

	
	//FIND SUCCESSOR
	public static final String PROTOCOL_FIND_SUCCESSOR = "findsucc";
	public static final String PROTOCOL_FIND_SUCCESSOR_KEY = "succkey";
	public static final String PROTOCOL_FIND_SUCCESSOR_SENDER_ADDR = "findsuccsenderaddr";
	
	//SUCCESSOR COMMAND (Used to pack different request under the same ring-logic in terms of message handling)
	public static final String PROTOCOL_FIND_SUCCESSOR_COMMAND = "findsucccommand";
	public static final String PROTOCOL_FIND_SUCCESSOR_COMMAND_FINGERTABLE = "findsucccommandfingertable";
	public static final String PROTOCOL_FIND_SUCCESSOR_COMMAND_LOOKUP = "findsucccommandlookup";
	public static final String PROTOCOL_FIND_SUCCESSOR_COMMAND_GET = "findsucccommandget";
	public static final String PROTOCOL_FIND_SUCCESSOR_COMMAND_PUT = "findsucccommandput";
	public static final String PROTOCOL_FIND_SUCCESSOR_COMMAND_REMOVE = "findsucccommandremove";
	public static final String PROTOCOL_FIND_SUCCESSOR_PUT_OBJECT = "findsucccommandputobject";
	
	//FIND SUCCESSOR RESPONSE
	public static final String PROTOCOL_FIND_SUCCESSOR_RESPONSE = "findsuccresp";
	public static final String PROTOCOL_FIND_SUCCESSOR_RESPONSE_ID = "findsuccrespid";
	public static final String PROTOCOL_FIND_SUCCESSOR_RESPONSE_ADDR = "findsuccrespaddr";
	public static final String PROTOCOL_FIND_SUCCESSOR_RESPONSE_OBJECT = "findsuccrespobject";

	//PREDECESSOR PINGING
	public static final String PROTOCOL_CHECK_PREDECESSOR = "ckpred";
	public static final String PROTOCOL_CHECK_PREDECESSOR_RESPONSE = "ckpredresp";
	
	//STATES
	public static final String STATE_DISCONNECTED = "disconnected";
	public static final String STATE_CONNECTING = "connecting";
	public static final String STATE_CONNECTED = "connected";
}
