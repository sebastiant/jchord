/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dht;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author sebastiantunstig
 */
public class Message {
    private String message;
    private Host host;
    private HostMap hostMap;
    private HashTable hashTable;
    
    //Protocol specific messages specifying whole or first part (if ended with #) of messages
    public static final String MESSAGE = "MESSAGE#";
    public static final String HELOMSG = "HELO#Version:"+Peer.VERSION+"#";
    public static final String WELCOME = "WELCOME#";
    public static final String PROPOSEIDFIRST = "PROPOSEID#1#";
    public static final String PROPOSEID = "PROPOSEID#";
    public static final String PROPOSALREPLY = "PROPOSALREPLY#";
    public static final String REQUESTHOSTMAP = "REQUESTHOSTMAP";
    public static final String HOSTMAPFOLLOWS = "HOSTMAPFOLLOWS#";
    public static final String HOSTMAPVERSION = "HOSTMAPVERSION#";
    public static final String HOST = "HOST#";
    public static final String PREDECESSOR = "PREDECESSOR#";
    public static final String FINGER = "FINGER#";
    public static final String GET = "GET#";
    public static final String PUT = "PUT#";
    public static final String REMOVE = "REMOVE#";
    public static final String DELIVER = "DELIVER#";
    public static final String STOREDATA = "STOREDATA#";
    public static final String BACKUPDATA = "BACKUPDATA#";
    
    static enum CommandName{
        PREDECESSOR, FINGER, MESSAGE, HELO, WELCOME, REQUESTHOSTMAP, HOSTMAPFOLLOWS, HOST, PROPOSEID, PROPOSALREPLY, HOSTMAPVERSION, GET, PUT, REMOVE, DELIVER, STOREDATA, BACKUPDATA;
    };
    
    public Message(String message, Host host, HostMap hostMap, HashTable hashTable){
        this.message=message;
        this.host=host;
        this.hostMap=hostMap;
        this.hashTable=hashTable;
    }
    
    private void handleSTOREDATA(String identifier, String load){
        Peer.notify(Peer.DEBUG, "Received STOREDATA with identifier: "+identifier + " and load: " + load);
    
    }
    private void handleBACKUPDATA(String identifier, String load){
        Peer.notify(Peer.DEBUG, "Received BACKUPDATA with identifier: "+identifier + " and load: " + load);
    }
    private void handleFINGER(int hostId, int listeningPort){
        Peer.notify(Peer.DEBUG, "Received finger from : #"+hostId + " with listeningPort: " + listeningPort);
        //This is expected to come from a new connection, so we should replace its hostmap-entry with this host.
        Host unconnectedHost=hostMap.getHostFromId(Peer.PEERWITHOUTID);
        if(unconnectedHost!=null){
            Host oldHost=hostMap.getHostFromId(hostId);
            if(oldHost!=null){
                unconnectedHost.setId(oldHost.getId());
                unconnectedHost.setIpAddress(oldHost.getIpAddress());
                unconnectedHost.setListeningPort(oldHost.getListeningPort());
                hostMap.removeHost(oldHost);
             }
            else{//If this peer is not already in our hostmap, this probably means that we're connecting to a network and got a finger earlier than we expected.
                unconnectedHost.setId(hostId);
                unconnectedHost.setListeningPort(listeningPort);
            }
        }        
    }
    private void handlePREDECESSOR(int hostId, int listeningPort){
        Peer.notify(Peer.DEBUG, "Received request for new predecessor from: #" + hostId);
        hostMap.getLocalhost().setPredecessorId(hostId);
        //Should send over data.
        System.out.println("predecessor set to: " + hostId);
        //If this came as a new connection, it does not have an ID yet, so we should replace its hostmap-entry with this host.
        Host unconnectedHost=hostMap.getHostFromId(Peer.PEERWITHOUTID);
        if(unconnectedHost!=null){
            Host oldHost=hostMap.getHostFromId(hostId);
            if(oldHost!=null){
                unconnectedHost.setId(oldHost.getId());
                unconnectedHost.setIpAddress(oldHost.getIpAddress());
                unconnectedHost.setListeningPort(oldHost.getListeningPort());
                hostMap.removeHost(oldHost);
            }
            else{//If this peer is not already in our hostmap, this probably means that we're connecting to a network and got a request earlier than we expected.
                unconnectedHost.setId(hostId);
                unconnectedHost.setListeningPort(listeningPort);
            }
        }
        //TODO: Send over the local datastore for it to use as our backup. Also, send over keys for it to be held responsible.
        //Send: storedata, backupdata
        HashMap<Integer,String> load=hashTable.getPredecessorData(hostMap.getLocalhost().getId()-1, hostId);
        if(load.size()>0){
            Peer.notify("Sending over storage the predecessor is responsible for.");
            for(int i=1;i<Peer.SIZEOFNODEIDENTIFIERSPACE;i++){
                if(load.get(i)!=null){
                    try{
                        Peer.sendMessage(new PrintWriter(host.getSocket().getOutputStream(), true), Integer.toString(host.getId()), STOREDATA+i+"#"+load.get(i));
                    }catch(IOException e){
                    }
                }
            }
        }
    }
    
    private void handleREQUESTHOSTMAP(){
        Peer.notify(Peer.DEBUG, "Received request for my hostmap, sending it out.");
        sendHostMap();
    }  
    private void handleHELO(String version, int listeningPort){
        if(!("HELO#"+version+"#").equals(HELOMSG)){
            Peer.notify("Warning! A Connecting peer does not run on same version as this peer!");
        }
        Host localhost = hostMap.getHostList().get(0);
        int rand1=Peer.getRandomId();
        if(hostMap.size()<3){
            Peer.notify(Peer.DEBUG, "This is the first connection!");
            int rand2=Peer.getRandomId();
            while(rand1==rand2){
                rand2=Peer.getRandomId();
            }
            localhost.setId(rand1);
            host.setId(rand2);
            host.setListeningPort(listeningPort);
            try{
                Peer.sendMessage(new PrintWriter(host.getSocket().getOutputStream(), true), Integer.toString(host.getId()), WELCOME+rand2+"#"+rand1);
            }catch(IOException e){
            }
            hostMap.setRequestStabilize(true);//Generate new predecessors / Successors.
            hostMap.incrementVersion();
            hostMap.broadcastVersion();
            return;
        }
        //Elsewise, we need to negotiate with the other connected peers to get a unique ID for the new peer.
        host.setListeningPort(listeningPort);
        int hostMapSize=hostMap.size();
        hostMap.startProposal(host, hostMap.getNumberOfConnectedHosts()-2); //all connected hosts except localhost and the new host.
        List<Integer> hostIdList=hostMap.getListOfHostIds();
        boolean ready=false;
        if(Peer.DEBUG){
            Peer.notify(Peer.DEBUG, "Trying to find an unused ID.");
        }
        while(!ready){
            for(int i=0;i<hostMapSize;i++){
                if(hostIdList.get(i)==rand1){
                    rand1=Peer.getRandomId();
                }else if(i==hostMapSize-1){
                    if(Peer.DEBUG){
                    Peer.notify(Peer.DEBUG, "found: " + rand1);
                    }
                    ready=true;
                }
            }
        }
        hostMap.multicastExceptHost(PROPOSEIDFIRST+rand1, host);
    }   
    private void handleHOSTMAPVERSION(int version){
        Peer.notify(Peer.DEBUG, "Received HOSTMAPVERSION. Version: " + version);
        if(version > hostMap.getVersion() && version > hostMap.getLatestMapVersionSeen()){
            hostMap.setLatestMapVersionSeen(version);
            Peer.notify("New host map available, (version: "+ version + ".) Requesting it.");
            try{
                Peer.sendMessage(new PrintWriter(host.getSocket().getOutputStream(), true), Integer.toString(host.getId()), REQUESTHOSTMAP);
            }catch(IOException e){
                Peer.notify("Could not send request for the lastest hostmap from host #" + host.getId());
            }
        }else if(version<hostMap.getVersion()){
            try{
                Peer.sendMessage(new PrintWriter(host.getSocket().getOutputStream(), true), Integer.toString(host.getId()), HOSTMAPVERSION+hostMap.getVersion());
            }catch(IOException e){
                Peer.notify("Could not send host map version to #" + host.getId());
            }
        }
    }
    private void handleMESSAGE(String message){
        System.out.println("<< ("+host.getId()+"): " +message+"\n> ");        
    }
    private void handleHOSTMAPFOLLOWS(int size){
        Peer.notify("Received HOSTMAPFOLLOWS. Size: " + size);
        hostMap.setHostMapIncoming(true);
        hostMap.setHostMapHostsLeft(size);
        hostMap.setHostMapUpdated(false);
    }
    private void handleWELCOME(int assignedId, int farEndId){
        Peer.notify(Peer.DEBUG, "Received WELCOME. assignedID: " + assignedId + " farEndID: " + farEndId);
        Peer.notify("Accepted to network with ID: " + assignedId);
        host.setId(farEndId);
        Host localhost=hostMap.getLocalhost();
        localhost.setId(assignedId);
    }
    private void handlePROPOSEID(int sequenceNumber, int proposedId){
        Peer.notify(Peer.DEBUG, "Received PROPOSEID. seq:" + sequenceNumber + " proposeid: " + proposedId);
        char verdict='y';//Changed to n if the proposal is denied.
        List<Integer> hostIdList=hostMap.getListOfHostIds();
        for(int i=0;i<hostIdList.size();i++){
            if(hostIdList.get(i)==proposedId){
                verdict='n';
            }
        }
        try{
            Peer.sendMessage(new PrintWriter(host.getSocket().getOutputStream(), true), "#"+Integer.toString(host.getId()), PROPOSALREPLY+sequenceNumber+"#"+proposedId+"#"+verdict);
        }catch(IOException e){
            Peer.notify("Could not reply on ID proposal to host id: " + host.getId());
        }
    }
    private void handlePROPOSALREPLY(int sequenceNumber, int proposedId, char verdict){
        Peer.notify(Peer.DEBUG, "Received PROPOSALREPLY. seq: " + sequenceNumber + " proposed ID: " + proposedId + " verdict: " + verdict);
        if(!hostMap.isProposalOnGoing()){
            Peer.notify("Received verdict of a proposal not expecting it.");
            return;
        }
        if(sequenceNumber!=hostMap.getProposalSequenceNumber()){ //A new proposal has already been sent out.
            return;
        }
        int hostsLeft=hostMap.getProposalHostsLeft()-1;
        hostMap.setProposalHostsLeft(hostsLeft);
        
        //Final reply, for the latest proposal. This means that this host tells the verdict, as the sequencenumber is the latest and therefore preceeding hosts were ok with the proposal.
        if(verdict=='y' && hostsLeft<1 && sequenceNumber==hostMap.getProposalSequenceNumber()){
            Peer.notify(Peer.DEBUG, "last verdict received.");
            
            Host newHost=hostMap.getProposedHost();
            if(newHost!=null){
                if(newHost.getId()==Peer.PEERWITHOUTID){
                    try{
                        Peer.notify("Concensus reached. inviting new peer, #" + proposedId + ", to network");
                        newHost.setId(proposedId);
                        Peer.sendMessage(new PrintWriter(newHost.getSocket().getOutputStream(), true), "#"+proposedId, "WELCOME#"+proposedId+"#"+hostMap.getLocalhost().getId());    
                        hostMap.stopProposal();
                        hostMap.setRequestStabilize(true);//Generate new predecessors / Successors.
                        hostMap.incrementVersion();
                        hostMap.broadcastVersion();
                        return;                

                    }catch(IOException e){
                        Peer.notify("Could not send host ID to new peer after reaching a verdict with the network peers.");
                    }
                    hostMap.stopProposal();
                }
            }
            
        }
        //If we got a no, create a new proposal and increment the sequencenumber.
        else if(verdict!='y'){
            hostMap.setProposalSequenceNumber(sequenceNumber+1);
            hostMap.setProposalHostsLeft(hostMap.getNumberOfConnectedHosts()-2); //all connected hosts except localhost and the new host.
            hostMap.multicastExceptHost(PROPOSEID+"#"+(sequenceNumber+1)+"#"+Peer.getRandomId(), hostMap.getProposedHost());
        }
    }
    private void handleHOST(int mapVersion, int hostId, String hostIp, int hostListeningPort){
        if(!hostMap.isHostMapIncoming()){
            Peer.notify("Received information of a host when not expecting it.");
            return;
        }
        if(mapVersion<hostMap.getVersion() || mapVersion < hostMap.getLatestMapVersionSeen()){
            return;
        }

        hostMap.getHostsReceived().add(hostId);
        int hostsLeft=hostMap.getHostMapHostsLeft()-1;
        hostMap.setHostMapHostsLeft(hostsLeft);
        List<Host> hostList = hostMap.getHostList();
 
        if(hostMap.getHostFromId(hostId)==null){
            hostMap.addHost(new Host(hostId, hostIp, hostListeningPort, null));
            Peer.notify("Added new host to host map. hostId: " + hostId + " hostIp: " + hostIp + " listningport: " + hostListeningPort);         
            hostMap.setHostMapUpdated(true);
        }

        if(hostsLeft<1){
            Peer.notify(Peer.DEBUG, "last host in hostmap received.");
            //Compare the list HostsReceived with current hostmap. if some host is missing, its been disconnected, so remove it from the local map.
            
            //TODO: Check if the host is really down before removing it?
            if(hostMap.getHostsReceived().size()<hostList.size()){
                List<Integer> hostsReceived=hostMap.getHostsReceived();
                for(int j=0;j<hostList.size();j++){
                    if(hostsReceived.indexOf(hostList.get(j).getId())==-1){
                        Peer.notify("Removing peer: "+hostList.get(j).getId()+ " after update of host map from other peers.");
                        hostMap.removeHost(hostList.get(j));
                        hostMap.setHostMapUpdated(true);
                    }
                }
            }
            hostMap.getHostsReceived().clear();
            hostMap.setHostMapIncoming(false);
            hostMap.setVersion(mapVersion);
            if(hostMap.isHostMapUpdated()){ //If any changes to our hostMap was done, broadcast the new version.
                hostMap.broadcastVersion();
            }
            Peer.notify("HostMap successfully updated to version: " + mapVersion);
            hostMap.setRequestStabilize(true);//Generate new predecessors / Successors.
        }
    }
    private void handleGET(int hostId, String identifier){
        Peer.notify(Peer.DEBUG, "Got a GET from: " + hostId+ "with key: "+identifier);
        int hashedKey=0;
        try{
            hashedKey=HashTable.generateKeyFromSHA1(identifier);
        }catch(Exception e){
            System.out.println("Could not compute hash from key:"+ identifier);
            return;
        }
        System.out.println("Hashed key: " +hashedKey);
        try{
            //If this is our responsibility
            if(hostMap.lookup(hashedKey)==hostMap.getLocalhost().getId()){
                System.out.println("Lookup is our responsibility. Sending back DELIVERY.");
                //Send a delivery-message with the hashtable contents, either directly to the requester (if connected) or using the fingertable.
                Host tempHost=hostMap.getHostFromId(hostId);
                if(tempHost.getSocket()!=null){
                    System.out.println("Directly connected to host, sending it to right destination right away!");
                    Peer.sendMessage(new PrintWriter(tempHost.getSocket().getOutputStream(), true), "#"+tempHost.getId(), DELIVER+ hostId+"#"+hashTable.getData(hashedKey));
                }else{
                    System.out.println("Not connected to host, using fingertable!");
                    tempHost=hostMap.getFingerTable().lookup(hostId);
                    Peer.sendMessage(new PrintWriter(tempHost.getSocket().getOutputStream(), true), "#"+tempHost.getId(), DELIVER+ hostId+"#"+hashTable.getData(hashedKey));
                }
                      
            
            }else{//We should pass the request along.
                System.out.println("Lookup is not our responsibility. Forwarding request along");
                //Check if we are connected to the host responsible for the key. Elsewise, send it the connected host responsible according to the fingertable.
                Host tempHost=hostMap.getHostFromId(hostMap.lookup(hashedKey));
                if(tempHost.getSocket()!=null){
                    System.out.println("Directly connected to host, sending it to right destination right away!");
                    Peer.sendMessage(new PrintWriter(tempHost.getSocket().getOutputStream(), true), "#"+tempHost.getId(), GET+hostId+"#"+identifier);
                }else{
                    System.out.println("Not connected to host, using fingertable!");
                    tempHost=hostMap.getFingerTable().lookup(hashedKey);
                    Peer.sendMessage(new PrintWriter(tempHost.getSocket().getOutputStream(), true), "#"+tempHost.getId(), GET+hostId+"#"+identifier);
                }
            }
        }catch(Exception e){
            System.out.println("Could not reply on/forward GET packet");
        }
    }
    private void handleREMOVE(String identifier){
        System.out.println("Got a REMOVE with key: "+identifier);
        int hashedKey;
        try{
            hashedKey=HashTable.generateKeyFromSHA1(identifier);
        }catch(Exception e){
            System.out.println("Could not compute hash from key:"+ identifier);
            return;
        }
        System.out.println("Hashed key: " +hashedKey);
        //If this is our responsibility:
        if(hostMap.lookup(hashedKey)==hostMap.getLocalhost().getId()){
            System.out.println("Removing item with identifier: "+identifier);
            hashTable.delData(hashedKey);
        }else{
            //Check if we are connected to the host responsible for the key. Elsewise, send it the connected host responsible according to the fingertable.
            Host tempHost=hostMap.getHostFromId(hostMap.lookup(hashedKey));
            if(tempHost!=null){
                try{
                    if(tempHost.getSocket()!=null){
                        System.out.println("Directly connected to host, sending it to right destination right away!");
                        Peer.sendMessage(new PrintWriter(tempHost.getSocket().getOutputStream(), true), "#"+tempHost.getId(), REMOVE+identifier);
                    }else{
                        System.out.println("Not connected to host, using fingertable!");
                        tempHost=hostMap.getFingerTable().lookup(hashedKey);
                        Peer.sendMessage(new PrintWriter(tempHost.getSocket().getOutputStream(), true), "#"+tempHost.getId(), REMOVE+identifier);
                    }
                }catch(Exception e){
                    System.out.println("Could not forward REMOVE packet");
                }
            }else{
                System.out.println("I cant pass the message along, unknown destination");
            }
        }
    }
    private void handlePUT(String identifier, String load){
        System.out.println("Got a PUT with identifier: "+identifier + " and load: " + load);
        int hashedKey;
        try{
            hashedKey=HashTable.generateKeyFromSHA1(identifier);
        }catch(Exception e){
            System.out.println("Could not compute hash from key:"+ identifier);
            return;
        }
        System.out.println("hasing resulted in hashedKey: " + hashedKey);
        //If it is our responsibility
        if(hostMap.lookup(hashedKey)==hostMap.getLocalhost().getId()){
            System.out.println("Our responsibility");
            hashTable.addData(hashedKey, load);
        }else{
            //Check if we are connected to the host responsible for the key. Elsewise, send it the connected host responsible according to the fingertable.
            Host tempHost=hostMap.getHostFromId(hostMap.lookup(hashedKey));
            if(tempHost!=null){
                try{
                    if(tempHost.getSocket()!=null){
                        System.out.println("Directly connected to host, sending it to right destination right away!");
                        Peer.sendMessage(new PrintWriter(tempHost.getSocket().getOutputStream(), true), "#"+tempHost.getId(), PUT+identifier+"#"+load);
                    }else{
                        System.out.println("Not connected to host, using fingertable!");
                        tempHost=hostMap.getFingerTable().lookup(hashedKey);
                        Peer.sendMessage(new PrintWriter(tempHost.getSocket().getOutputStream(), true), "#"+tempHost.getId(), PUT+identifier+"#"+load);
                    }
                }catch(Exception e){
                    System.out.println("Could not forward PUT packet");
                }
            }else{
                System.out.println("I cant pass the message along, unknown destination");
            }
        }
    
    }
    private void handleDELIVER(int hostId, String load){
        Peer.notify(Peer.DEBUG, "Got a DELIVER for host: "+hostId);
        if(hostId==hostMap.getLocalhost().getId()){
            System.out.println("\n(*)Lookup resulted in following data: "+ load + "\n> ");
        }else{
            //Check if we are connected to the host responsible for the key. Elsewise, send it the connected host responsible according to the fingertable.
            Host tempHost=hostMap.getHostFromId(hostId);
            if(tempHost!=null){
                try{
                    if(tempHost.getSocket()!=null){
                        System.out.println("Directly connected to host, sending it to right destination right away!");
                        Peer.sendMessage(new PrintWriter(tempHost.getSocket().getOutputStream(), true), "#"+tempHost.getId(), DELIVER+hostId+"#"+load);
                    }else{
                        System.out.println("Not connected to host, using fingertable!");
                        tempHost=hostMap.getFingerTable().lookup(hostId);
                        Peer.sendMessage(new PrintWriter(tempHost.getSocket().getOutputStream(), true), "#"+tempHost.getId(), DELIVER+hostId+"#"+load);
                    }
                }catch(Exception e){
                    System.out.println("Could not forward DELIVER packet");
                }
            }else{
                System.out.println("I cant pass the message along, unknown destination");
            }
        }
    }

    private void sendHostMap(){
        try{
            Peer.sendMessage(new PrintWriter(host.getSocket().getOutputStream(), true), Integer.toString(host.getId()), HOSTMAPFOLLOWS+hostMap.size());
        }catch(IOException e){
            Peer.notify("Could not send hostmapfollows #" + host.getId());
        }
        List<Host> hostList = hostMap.getHostList();
        Host tempHost;
        
        int hostMapVersion;
        int hostId;
        String hostIp;
        int hostListeningPort;
        for(int i=0;i<hostMap.size();i++){
            StringBuilder msg = new StringBuilder();
            tempHost=hostList.get(i);
            msg.append(HOST);
            hostMapVersion=hostMap.getVersion();
            hostId=tempHost.getId();
            hostIp=tempHost.getIpAddress();
            hostListeningPort=tempHost.getListeningPort();
            msg.append(hostMapVersion);
            msg.append("#");
            msg.append(hostId);
            msg.append("#");
            msg.append(hostIp);
            msg.append("#");
            msg.append(hostListeningPort);
            try{
                Peer.sendMessage(new PrintWriter(host.getSocket().getOutputStream(), true), Integer.toString(host.getId()), msg.toString());
            }catch(IOException e){
                Peer.notify("Could not send information of a host to host #" + host.getId());
            }
        }
    }
    private void executeCommand(Command command){
        if (command == null) {
                return;
        }
        switch (command.getCommandName()) {
        case REQUESTHOSTMAP:
            handleREQUESTHOSTMAP();
            return;
        }
        // all further commands requires atleast one argument
        String arg1 = command.getArg1();
        if (arg1 == null) {
                Peer.notify("Message received not following protocol! (arg1)");
                return;
        }
        int hostId;
        String identifier;
        switch (command.getCommandName()) {
            case MESSAGE:
                String receivedMessage=arg1;
                handleMESSAGE(receivedMessage);
                return;
            case HOSTMAPVERSION:
                int version;
                try{
                   version=Integer.parseInt(arg1);
                }catch(NumberFormatException e){
                    Peer.notify("Message received not following protocol! (HOSTMAPVERSION)");
                    return;
                }
                handleHOSTMAPVERSION(version);
                return;
            case HOSTMAPFOLLOWS:
                int size;
                try{
                   size=Integer.parseInt(arg1);
                }catch(NumberFormatException e){
                    Peer.notify("Message received not following protocol!(HOSTMAPFOLLOWS)");
                    return;
                }
                handleHOSTMAPFOLLOWS(size);
                return;
            case REMOVE:
                try{
                   identifier=arg1;
                }catch(NumberFormatException e){
                    Peer.notify("Message received not following protocol!(REMOVE)");
                    return;
                }
                handleREMOVE(identifier);
                return;
        }
        //all further commands require atleast two arguments
        String arg2 = command.getArg2();
        if (arg2 == null) {
                Peer.notify("Message received not following protocol!(arg2)");
                return;
        }
        String load;
        int listeningPort;
        switch (command.getCommandName()) {
            case FINGER:
                try{
                   hostId=Integer.parseInt(arg1);
                   listeningPort=Integer.parseInt(arg2);
                }catch(NumberFormatException e){
                    Peer.notify("Message received not following protocol!(Finger)");
                    return;
                }
                handleFINGER(hostId, listeningPort);
                return;
            case PREDECESSOR:
                try{
                   hostId=Integer.parseInt(arg1);
                   listeningPort=Integer.parseInt(arg2);
                }catch(NumberFormatException e){
                    Peer.notify("Message received not following protocol!(PREDECESSOR)");
                    return;
                }
                handlePREDECESSOR(hostId, listeningPort);
                return;
            case HELO:
                String version=arg1;
                try{
                    listeningPort=Integer.parseInt(arg2);
                }catch(NumberFormatException e){
                    Peer.notify("Message received not following protocol!(HELO)");
                    return;
                }
                handleHELO(version, listeningPort);
                return;
            case WELCOME:
                int assignedId;
                int farEndId;
                try{
                   assignedId=Integer.parseInt(arg1);
                   farEndId=Integer.parseInt(arg2);
                }catch(NumberFormatException e){
                    Peer.notify("Message received not following protocol!(WELCOME)");
                    return;
                }
                handleWELCOME(assignedId, farEndId);
                return;
            case PROPOSEID:
                int sequenceNumber;
                int proposedId;
                try{
                   sequenceNumber=Integer.parseInt(arg1);
                   proposedId=Integer.parseInt(arg2);
                }catch(NumberFormatException e){
                    Peer.notify("Message received not following protocol!(PROPOSEID)");
                    return;
                }
                handlePROPOSEID(sequenceNumber, proposedId);
                return;
            case GET:
                try{
                   hostId=Integer.parseInt(arg1);
                   identifier=arg2;
                }catch(NumberFormatException e){
                    Peer.notify("Message received not following protocol!(GET)");
                    return;
                }
                handleGET(hostId, identifier);
                return;
            case PUT:
                identifier=arg1;
                load=arg2;
                handlePUT(identifier, load);
                return;
            case DELIVER:
                try{
                   hostId=Integer.parseInt(arg1);
                   load=arg2;
                }catch(NumberFormatException e){
                    Peer.notify("Message received not following protocol!(DELIVER)");
                    return;
                }
                handleDELIVER(hostId,load);
                return;
            case STOREDATA:
                identifier=arg1;
                load=arg2;
                handleSTOREDATA(identifier,load);
                return;
            case BACKUPDATA:
                identifier=arg1;
                load=arg2;
                handleBACKUPDATA(identifier, load);
                return;
        }
        //all further commands require atleast three arguments
        String arg3 = command.getArg3();
        if (arg3 == null) {
                Peer.notify("Message received not following protocol!(arg3)");
                return;
        }
        switch (command.getCommandName()) {
            case PROPOSALREPLY:
                int sequenceNumber;
                int proposedId;
                char verdict;
                try{
                   sequenceNumber=Integer.parseInt(arg1);
                   proposedId=Integer.parseInt(arg2);
                   verdict=arg3.charAt(0);
                }catch(NumberFormatException e){
                    Peer.notify("Message received not following protocol!(PROPOSALREPLY)");
                    return;
                }catch(IndexOutOfBoundsException e){
                    Peer.notify("Message received not following protocol!(PROPOSALREPLY)");
                    return;
                }
                handlePROPOSALREPLY(sequenceNumber, proposedId, verdict);
                return;
        }
        //all further commands require atleast four arguments
        String arg4 = command.getArg4();
        if (arg4 == null) {
                Peer.notify("Message received not following protocol!(arg4)");
                return;
        }
        switch (command.getCommandName()) {
            case HOST:
                int version;
                String hostIp;
                int hostListeningPort;
                try{
                   version=Integer.parseInt(arg1);
                   hostId=Integer.parseInt(arg2);
                   hostIp=arg3;
                   hostListeningPort=Integer.parseInt(arg4);
                }catch(NumberFormatException e){
                    Peer.notify("Message received not following protocol!(HOST)");
                    return;
                }
                handleHOST(version, hostId, hostIp, hostListeningPort);
                return;
            default:
                Peer.notify("Message received not following protocol!(Default)");
        }
    }
    private Command parse(String userInput) {
        	if (userInput == null) {
			return null;
		}

		StringTokenizer tokenizer = new StringTokenizer(userInput, "#");
		if (tokenizer.countTokens() == 0) {
			return null;
		}

		CommandName commandName = null;
		String arg1=null;
                String arg2=null;
                String arg3=null;
                String arg4=null;
		int userInputTokenNo = 1;

		while (tokenizer.hasMoreTokens()) {
			switch (userInputTokenNo) {
                            case 1:
                                    try {
                                            String commandNameString = tokenizer.nextToken();
                                            commandName = CommandName.valueOf(CommandName.class, commandNameString);
                                    } catch (IllegalArgumentException e) {
                                            Peer.notify("Message received not following protocol!(parser)");
                                            return null;
                                    }
                                    break;
                            case 2:
                                    arg1 = tokenizer.nextToken();
                                    break;
                            case 3:
                                    arg2 = tokenizer.nextToken();
                                    break;
                            case 4:
                                    arg3 = tokenizer.nextToken();
                                    break;
                            case 5:
                                    arg4 = tokenizer.nextToken();
                                    break;
                            default:
                                    Peer.notify("Message received not following protocol!(parser default)");
                                    return null;
			}
			userInputTokenNo++;
		}
		return new Command(commandName, arg1, arg2, arg3, arg4);
    }
    public void handleMessage(){
        Command command = parse(message);
        executeCommand(command);
    }
    private class Command{
        private CommandName commandName;
        private String arg1;
        private String arg2;
        private String arg3;
        private String arg4;
        private Command(CommandName commandName, String arg1, String arg2, String arg3, String arg4) {
                this.commandName = commandName;
                this.arg1 = arg1;
                this.arg2 = arg2;
                this.arg3 = arg3;
                this.arg4 = arg4;
        }
        
        private CommandName getCommandName() {
                return commandName;
        }
        private String getArg1() {
                return arg1;
        }
        private String getArg2() {
                return arg2;
        }
        private String getArg3() {
                return arg3;
        }
        private String getArg4() {
                return arg4;
        }
    }
}
