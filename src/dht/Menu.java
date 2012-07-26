/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dht;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

/**
 *
 * @author sebastiantunstig
 */
public class Menu implements Runnable{
    Peer peer;
    
    public Menu(Peer peer){
        this.peer=peer;
    }
    static enum CommandName{
        m, map, d, disconnect, b, broadcast, c, connect, p, put, g, get, r, remove, l, lookup, s, status, f, fingertable, h, help, q, quit;
    };
    static String commandNamesHelp = "Available commands:\n\t (m)ap\t\t\t\t Show current map of peers.\n\t (d)isconnect\t\t\t Disconnect from all hosts.\n\t "
            + "(b)roadcast <message>\t\t Broadcast message to all connected hosts.\n\t (c)onnect <ip-addr> <port>\t Connect to a host, given an IP-address.\n\t "
            + "(l)ook up <key> \t\t Looks up the ID of the host responsible for the specified key.\n\t (p)ut <key> <data>\t\t Adds a mapping of a key and a load of data to the DHT\n\t "
            + "(r)emove <key>\t\t Removes any data associated with a key from the DHT\n\t (g)et <key>\t\t Retrieves any data associated with the key from the DHT\n\t "
            + "(s)tatus\t\t\t Show peer status.\n\t (f)ingertable\t\t\t Show the current fingertable.\n\t (h)elp\t\t\t\t Show this help.\n\t (q)uit\t\t\t\t Disconnect from all hosts and stop execution.";
    @Override
    public void run(){
        try {
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            String inputString;
            System.out.print("> ");
            while(peer.isActive() && (inputString=stdin.readLine())!=null){
                execute(parse(inputString));
                System.out.print("> ");
            }
        }catch (IOException e) {
            System.err.println("Problem reading from stdin");
        }
    }
    private class Command {
		private CommandName commandName;
		private String arg1;
		private String arg2;
                
		private CommandName getCommandName() {
			return commandName;
		}
		private String getArg1() {
			return arg1;
		}
		private String getArg2() {
			return arg2;
		}
		private Command(CommandName commandName, String arg1, String arg2) {
			this.commandName = commandName;
			this.arg1 = arg1;
                        this.arg2 = arg2;
		}
    }
    private Command parse(String userInput) {
        		if (userInput == null) {
			return null;
		}

		StringTokenizer tokenizer = new StringTokenizer(userInput);
		if (tokenizer.countTokens() == 0) {
			return null;
		}

		CommandName commandName = null;
		String arg1 = null;
                String arg2 = null;
		int userInputTokenNo = 1;

		while (tokenizer.hasMoreTokens()) {
                    switch (userInputTokenNo) {
                        case 1:
                                try {
                                        String commandNameString = tokenizer.nextToken();
                                        commandName = CommandName.valueOf(CommandName.class, commandNameString);
                                } catch (IllegalArgumentException e) {
                                        System.out.println("Illegal command! Use 'help' for a listing of available commands.");
                                        return null;
                                }
                                break;
                        case 2:
                                arg1 = tokenizer.nextToken();
                                break;
                        case 3:
                                arg2 = tokenizer.nextToken();
                                break;
                        default:
                                System.out.println("Illegal command! Use 'help' for a listing of available commands.");
                                return null;
                        }
                        userInputTokenNo++;
		}
		return new Command(commandName, arg1, arg2);
    }
    void execute(Command command){
		if (command == null) {
			return;
		}
		switch (command.getCommandName()) {
                    case m:
                    case map:
                        System.out.println("Printing network map of peers. \"(*)\" indicates that a connection is established to the peer. \"(#)\" indicates that the peer is the local peer.");
                        System.out.println(peer.getHostMap().toString());
                        return;
                    case d:
                    case disconnect:
                        System.out.println("disconnecting");
                        if(peer.getHostMap().size()>1){
                            peer.getHostMap().disconnectAll();
                            peer.getHostMap().clearHostMap();
                            peer.getHostMap().getHostList().get(0).setId(Peer.UNCONNECTED);
                            peer.getHostMap().getHostList().get(0).setPredecessorId(Peer.UNCONNECTED);
                            peer.getHostMap().getHostList().get(0).setSuccessorId(Peer.UNCONNECTED);
                        }
                        return;
                    case s:
                    case status:
                        System.out.println("current succ: " + peer.getHostMap().getHostList().get(0).getSuccessorId() + ", desired succ: " + peer.getHostMap().getDesiredSuccessor());
                        System.out.println("Current pred: " + peer.getHostMap().getHostList().get(0).getPredecessorId());
                        System.out.println("Host Map version: "+peer.getHostMap().getVersion());
                        System.out.println("Session threads in use: " + peer.getThreadPool().size());
                        System.out.println("Amount of connected sessions: " + peer.getHostMap().getNumberOfConnectedHosts());
                        System.out.println("Total number of hosts in Host Map: " + peer.getHostMap().size());
                        return;
                    case h:
                    case help:
                        System.out.println(commandNamesHelp);
                        return;
                    case f:
                    case fingertable:
                        System.out.print(peer.getHostMap().getFingerTable().toString());
                        System.out.println("Successor for key 12: ");
                        return;
                    case q:
                    case quit:
                        peer.setActive(false);
                        return;
                    }
                    // all further commands requires atleast one argument
                    String arg1 = command.getArg1();
                    Host localhost;
                    if (arg1 == null) {
                            System.out.println("Illegal command! Use 'help' for a listing of available commands.");
                            return;
                    }
                    Connection connection;
                    String key;
                    switch (command.getCommandName()) {
                    case b:
                    case broadcast:
                        peer.getHostMap().broadcast(Message.MESSAGE+arg1);
                        return;
                    case l:
                    case lookup:
                        key=arg1;
                        try{
                            int hashedKey=HashTable.generateKeyFromSHA1(key);
                            System.out.println("Hashed value: "+hashedKey+"\n Responsible host: "+peer.getHostMap().lookup(hashedKey));
                        }catch(Exception e){
                            Peer.notify("Error! Could not get hashed value or lookup key successor");
                        }
                        return;
                    case g:
                    case get:
                        key=arg1;
                        localhost=peer.getHostMap().getLocalhost();
                        try{
                            int hashedKey=HashTable.generateKeyFromSHA1(key);
                            if(peer.getHostMap().lookup(hashedKey)==localhost.getId()){
                                if(peer.getHashTable().getData(hashedKey)==null){
                                    System.out.println("(*) No data associated with key: " + key);
                                }else
                                    System.out.println("(*) Payload: " + peer.getHashTable().getData(hashedKey));
                            }
                            else{
                                Peer.notify("Lookup is not our responsibility. Forwarding request along (hashvalue of key: " + hashedKey+ " makes successor: "+peer.getHostMap().lookup(hashedKey) +")");
                                //Check if we are connected to the host responsible for the key. Elsewise, send it the connected host responsible according to the fingertable.
                                Host tempHost=peer.getHostMap().getHostFromId(peer.getHostMap().lookup(hashedKey));
                                if(tempHost.getSocket()!=null){
                                    Peer.notify(Peer.DEBUG, "Directly connected to host, sending it to right destination right away!");
                                }else{
                                    Peer.notify(Peer.DEBUG, "Not connected to host, using fingertable!");
                                    tempHost=peer.getHostMap().getFingerTable().lookup(hashedKey);
                                }
                                Peer.sendMessage(new PrintWriter(tempHost.getSocket().getOutputStream(), true), "#"+tempHost.getId(), Message.GET+peer.getHostMap().getLocalhost().getId()+"#"+key);
                            }
                        }catch(Exception e){
                            Peer.notify("Error! Could not forward request.");
                            return;
                        }
                        return;
                    case r:
                    case remove:
                        key=arg1;
                        localhost=peer.getHostMap().getLocalhost();
                        try{
                            int hashedKey=HashTable.generateKeyFromSHA1(key);
                            if(peer.getHostMap().lookup(hashedKey)==localhost.getId()){ //If this is our responsibility
                                peer.getHashTable().delData(hashedKey);
                            }
                            else{
                                Peer.notify("Lookup is not our responsibility. Forwarding request along (hashvalue of key: " + hashedKey+ ")");
                                //Check if we are connected to the host responsible for the key. Elsewise, send it the connected host responsible according to the fingertable.
                                Host tempHost=peer.getHostMap().getHostFromId(peer.getHostMap().lookup(hashedKey));
                                if(tempHost.getSocket()!=null){
                                    Peer.notify(Peer.DEBUG, "Directly connected to host, sending it to right destination right away!");
                                }else{
                                    Peer.notify(Peer.DEBUG, "Not connected to host, using fingertable!");
                                    tempHost=peer.getHostMap().getFingerTable().lookup(hashedKey);
                                }
                                Peer.sendMessage(new PrintWriter(tempHost.getSocket().getOutputStream(), true), "#"+tempHost.getId(),Message.REMOVE+key);
                            }
                        }catch(Exception e){
                            Peer.notify("Error! Could not forward request.");
                            return;
                        }
                        return;
                    }
                    String arg2 = command.getArg2();
                    if (arg2 == null) {
                            System.out.println("Illegal command! Use 'help' for a listing of available commands.");
                            return;
                    }
                    switch (command.getCommandName()) {
                        case c:
                        case connect:
                            int port;
                            try{
                                port=Integer.parseInt(arg2);
                            }catch(NumberFormatException e){
                                System.out.println("Illegal command! Use 'help' for a listing of available commands.");
                                return;
                            }
                            if((arg1.equals("localhost") || arg1.equals("127.0.0.1")) && port==peer.getListeningPort()){
                                System.out.println("Can't connect to this running peer.");
                                return;
                            }
                            connection = new Connection(peer, arg1, port, peer.getListeningPort(), peer.getThreadPool(), peer.getHostMap(), peer.getHashTable());
                            try{
                                if(connection.prepareOutgoingConnection()){
                                    connection.createOutoingConnection();
                                }
                            }catch(UnknownHostException e){
                                Peer.notify("Could not find a host with that IP-address / domain name.");
                            }catch(IOException e){
                                Peer.notify("Could not connect to the host on the given port.");
                            }
                            return;
                        case p:
                        case put:
                            key=arg1;
                            String load=arg2;
                            localhost=peer.getHostMap().getLocalhost();
                            try{
                                int hashedKey=HashTable.generateKeyFromSHA1(key);
                                if(peer.getHostMap().lookup(hashedKey)==localhost.getId()){ //If this is our responsibility
                                    peer.getHashTable().addData(hashedKey, load);
                                }
                                else{
                                    System.out.println("Lookup is not our responsibility. Forwarding request along");
                                    //Check if we are connected to the host responsible for the key. Elsewise, send it the connected host responsible according to the fingertable.
                                    Host tempHost=peer.getHostMap().getHostFromId(peer.getHostMap().lookup(hashedKey));
                                    if(tempHost.getSocket()!=null){
                                        System.out.println("Directly connected to host, sending it to right destination right away!");
                                    }else{
                                        System.out.println("Not connected to host, using fingertable!");
                                        tempHost=peer.getHostMap().getFingerTable().lookup(hashedKey);
                                    }
                                    Peer.sendMessage(new PrintWriter(tempHost.getSocket().getOutputStream(), true),"#"+tempHost.getId(),Message.PUT+key+"#"+load);
                                }
                            }catch(Exception e){
                                System.out.println("Could not forward request.");
                                return;
                            }
                            return;
                        default:
                            System.out.println("Illegal command! Use 'help' for a listing of available commands.");
		}
	}
        
}