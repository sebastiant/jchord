package com.github.sebastiant.jchord.overlay;

public interface Protocol {
	//COMMAND SPECIFIER
	String PROTOCOL_COMMAND = "comm";
	
	//JOINING
	String PROTOCOL_JOIN = "join";
	String PROTOCOL_JOIN_DENIED = "joindenied";
	String PROTOCOL_JOIN_ID = "joinid";
	String PROTOCOL_JOIN_ARITY = "joinarity";
	String PROTOCOL_JOIN_IDENTIFIERSPACE = "joinidspace";	
	String PROTOCOL_DENIED = "denied";
	
	//SUCCESSOR INFORM
	String PROTOCOL_SUCCESSORINFORM = "succ";
	
	//PREDECESSOR REQUEST/RESPONSE
	String PROTOCOL_PREDECESSOR_RESPONSE = "predresponse";
	String PROTOCOL_PREDECESSOR_REQUEST = "predreq";
	String PROTOCOL_PREDECESSOR_ID = "predid";
	String PROTOCOL_PREDECESSOR_ADDRESS = "predaddr";
	String PROTOCOL_SENDER_ID = "sid";
	String PROTOCOL_NULL = "null";
	
	//SUCCESSOR LIST
	String PROTOCOL_SUCCESSORLIST_1_ADDR = "succlist1addr";
	String PROTOCOL_SUCCESSORLIST_1_ID = "succlist1id";
	String PROTOCOL_SUCCESSORLIST_2_ADDR = "succlist2addr";
	String PROTOCOL_SUCCESSORLIST_2_ID = "succlist2id";
	String PROTOCOL_SUCCESSORLIST_3_ADDR = "succlist3addr";
	String PROTOCOL_SUCCESSORLIST_3_ID = "succlist3id";

	//FIND SUCCESSOR
	String PROTOCOL_FIND_SUCCESSOR = "findsucc";
	String PROTOCOL_FIND_SUCCESSOR_KEY = "succkey";
	String PROTOCOL_FIND_SUCCESSOR_SENDER_ADDR = "findsuccsenderaddr";
	
	//SUCCESSOR COMMAND (Used to pack different request under the same ring-logic in terms of message handling)
	String PROTOCOL_FIND_SUCCESSOR_COMMAND = "findsucccommand";
	String PROTOCOL_FIND_SUCCESSOR_COMMAND_FINGERTABLE = "findsucccommandfingertable";
	String PROTOCOL_FIND_SUCCESSOR_COMMAND_LOOKUP = "findsucccommandlookup";
	String PROTOCOL_FIND_SUCCESSOR_COMMAND_GET = "findsucccommandget";
	String PROTOCOL_FIND_SUCCESSOR_COMMAND_PUT = "findsucccommandput";
	String PROTOCOL_FIND_SUCCESSOR_COMMAND_REMOVE = "findsucccommandremove";
	String PROTOCOL_FIND_SUCCESSOR_PUT_OBJECT = "findsucccommandputobject";
	
	//FIND SUCCESSOR RESPONSE
	String PROTOCOL_FIND_SUCCESSOR_RESPONSE = "findsuccresp";
	String PROTOCOL_FIND_SUCCESSOR_RESPONSE_ID = "findsuccrespid";
	String PROTOCOL_FIND_SUCCESSOR_RESPONSE_ADDR = "findsuccrespaddr";
	String PROTOCOL_FIND_SUCCESSOR_RESPONSE_OBJECT = "findsuccrespobject";

	//DATA RESPONSIBILITY
	String PROTOCOL_DATA_RESPONSIBILITY = "dataresponsibility";
	String PROTOCOL_DATA_KEY = "datakey";
	String PROTOCOL_DATA_OBJECT = "dataobject";

	//PREDECESSOR PINGING
	String PROTOCOL_CHECK_PREDECESSOR = "ckpred";
	String PROTOCOL_CHECK_PREDECESSOR_RESPONSE = "ckpredresp";
	
	//STATES
	String STATE_DISCONNECTED = "disconnected";
	String STATE_CONNECTING = "connecting";
	String STATE_CONNECTED = "connected";
}
