/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dht;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author sebastiantunstig
 */

public class HostMap {
    private boolean disconnectAll;
    private List<Host> hostList;
    private int version;
    private int desiredSuccessor;
    //Fingertable used to enable log(n) messages required to find a host responsible for a key.
    private FingerTable fingerTable;
    
    
    //Used when reaching concenssus for a new host's unique ID
    private Host proposedHost;
    private boolean proposalOnGoing;
    private int proposalSequenceNumber;
    private int proposalHostsLeft;
    
    //Used when information of new hosts are received and the hostmap is updated.
    private boolean hostMapIncoming;
    private int latestMapVersionSeen;
    private boolean hostMapUpdated;
    private int hostMapHostsLeft;
    private List<Integer> hostsReceived;

    
    //Set to true by a session if a stabilize is requested.
    private boolean requestStabilize;
    
    public HostMap(){
        disconnectAll=false;
        version = 1;
        desiredSuccessor=Peer.UNCONNECTED;
        hostList = new LinkedList<Host>();
        fingerTable=new FingerTable(3,this);
        //from globalmessagebuffer
        latestMapVersionSeen = 1;
        hostsReceived = new LinkedList<Integer>();
        hostMapIncoming=false;
        this.requestStabilize=false;
    }
    
    public List<Host> getHostList() {
        return hostList;
    }
    public FingerTable getFingerTable() {
        return fingerTable;
    }
    public boolean isDisconnectAll() {
        return disconnectAll;
    }
    public synchronized void setDisconnectAll(boolean disconnectAll){
        this.disconnectAll=disconnectAll;
    }
    public synchronized void disconnectAll() {
        this.disconnectAll = true;
        for(int i=0;i<size();i++){
            hostList.get(i).setDisconnectFrom(true);
        }
    }
    public int getDesiredSuccessor() {
        return desiredSuccessor;
    }
    public void addHost(Host host){
        hostList.add(host);
    }
    public int size(){
        return hostList.size();
    }
    public int getVersion() {
        return version;
    }
    public synchronized void incrementVersion(){
        version++;
    } 
    public synchronized void setVersion(int version) {
        this.version = version;
    }   
    public synchronized void removeHost(Host host){
        try{
            hostList.remove(host);
        }catch(Exception e){
        }
    }
    public synchronized void clearHostMap(){
        for(int i=0;i<size();i++){
            hostList.remove(1);
        }
    }  
    public Host getLocalhost(){
        return hostList.get(0);
    }
    public int getNumberOfConnectedHosts(){
        int hostMapSize=size();
        int connectedHosts=0;
        Host tempHost;
        for(int i=0;i<hostMapSize;i++){
            tempHost=hostList.get(i);
            if(tempHost.getSocket()!=null){
                connectedHosts++;
            }
        }  
        return connectedHosts;
    }
    public void broadcast(String msg){
        System.out.print("\n(broadcasting): \"" + msg + "\"\n > ");
        Host tempHost;
        if(size()>1){
            for(int i=1;i<size();i++){
                tempHost=hostList.get(i);
                if(tempHost.getSocket()!=null){
                    try{
                        Peer.sendMessage(new PrintWriter(tempHost.getSocket().getOutputStream(), true), Integer.toString(tempHost.getId()), msg);
                    }catch(IOException e){
                        Peer.notify("Error while broadcasting to host: #"+tempHost.getId());
                    }
                }
            }
        }
    }
    public void broadcastVersion(){
        broadcast(Message.HOSTMAPVERSION+version);
    }
    public void multicastExceptHost(String msg, Host host){
        Peer.notify(Peer.DEBUG,"About to multicast: \"" + msg + "\" to everyone except host #" + host.getId());
        int hostListSize=getHostList().size();
        Host tempHost;
        PrintWriter pw;
        if(hostListSize>1){
            for(int i=1;i<hostListSize;i++){
                tempHost=hostList.get(i);
                if(!tempHost.equals(host) && (tempHost.getSocket()!=null)){
                    try{
                    pw = new PrintWriter(tempHost.getSocket().getOutputStream(), true);
                    Peer.sendMessage(pw, Integer.toString(tempHost.getId()), msg);
                    }catch(IOException e){

                    }
                }
            }
        }
    } 
    //Find the first clockwise node ID from this node in the hostmap.
    public synchronized void updateDesiredSuccessor(){
        System.out.println("Updating desired successor");
        int hosts=hostList.size();
        if(hosts<2){
            return;
        }
        
        List<Integer> hostIdList=new LinkedList<Integer>();
        for(int i=0;i<hosts;i++){
            hostIdList.add(hostList.get(i).getId());
        }

        int id=hostList.get(0).getId();
        int succ=id+1;
        int index;
        for(int j=0;j<Peer.SIZEOFNODEIDENTIFIERSPACE-1;j++){
            if(succ==Peer.SIZEOFNODEIDENTIFIERSPACE+1)
                succ=1;
            index=hostIdList.indexOf(succ);
            if(index!=-1){
                desiredSuccessor=succ;
                return;
            }
            succ++;
        }
    }   
    public List<Integer> getListOfHostIds(){
        List<Integer> hostIdList=new LinkedList<Integer>();
        for(int i=0;i<hostList.size();i++){
            hostIdList.add(hostList.get(i).getId());            
        }
        return hostIdList;
    }

    public  int lookup(int key){
        if(hostList.size()==1)
            return Peer.UNCONNECTED;
        //iterate through the list of host ID's and pick the closest host ID counting clockwise from (and including) the key.
        List<Integer> hostIdList=getListOfHostIds();
        int current=key;
        for(int i=0;i<Peer.SIZEOFNODEIDENTIFIERSPACE;i++){
            if(current>Peer.SIZEOFNODEIDENTIFIERSPACE){//"current modulo size"
                current=1;
            }
            if(hostIdList.indexOf(current)!=-1){
                Peer.notify(Peer.DEBUG, "lookup resulted in host ID: "+current);
                return current;
            }
            current++;
        }
        return -1;
    }
    public Host getHostFromId(int hostId){
        Host tempHost;
        for(int i=0;i<size();i++){
            tempHost=hostList.get(i);
            if(tempHost.getId()==hostId){
                return tempHost;
            }
        }
        return null;
    }    
    public boolean isHostMapIncoming() {
        return hostMapIncoming;
    }
    public synchronized void setHostMapIncoming(boolean hostMapIncoming) {
        this.hostMapIncoming = hostMapIncoming;
    }
    public int getLatestMapVersionSeen() {
        return latestMapVersionSeen;
    }
    public void setLatestMapVersionSeen(int latestMapVersionSeen) {
        this.latestMapVersionSeen = latestMapVersionSeen;
    }
    public boolean isHostMapUpdated() {
        return hostMapUpdated;
    }
    public synchronized void setHostMapUpdated(boolean hostMapUpdated) {
        this.hostMapUpdated = hostMapUpdated;
    }
    public int getHostMapHostsLeft() {
        return hostMapHostsLeft;
    }
    public synchronized void setHostMapHostsLeft(int hostMapHostsLeft) {
        this.hostMapHostsLeft = hostMapHostsLeft;
    }
    public synchronized List<Integer> getHostsReceived() {
        return hostsReceived;
    }
    public synchronized void startProposal(Host host, int hosts){
        this.proposalOnGoing=true;
        this.proposedHost=host;
        this.proposalSequenceNumber=1;
        this.proposalHostsLeft=hosts;
        
    }
    public synchronized void stopProposal(){
        this.proposalOnGoing=false;
        this.proposedHost=null;
        this.proposalSequenceNumber=1;
        this.proposalHostsLeft=0;
        
    }
    public Host getProposedHost() {
        return proposedHost;
    }
    public int getProposalHostsLeft() {
        return proposalHostsLeft;
    }
    public synchronized void setProposalHostsLeft(int proposalHostsLeft) {
        this.proposalHostsLeft = proposalHostsLeft;
    }
    public int getProposalSequenceNumber() {
        return proposalSequenceNumber;
    }
    public synchronized void setProposalSequenceNumber(int proposalSequenceNumber) {
        this.proposalSequenceNumber = proposalSequenceNumber;
    }
    public boolean isProposalOnGoing() {
        return proposalOnGoing;
    }
    public boolean isRequestStabilize() {
        return requestStabilize;
    }
    public synchronized void setRequestStabilize(boolean requestStabilize) {
        this.requestStabilize = requestStabilize;
    }
    
    @Override
    public String toString(){  
        int hostListSize=size();
        Host tempHost;
        Boolean isLocalhost;
        StringBuilder result = new StringBuilder();
        System.out.println("Predecessor: " + hostList.get(0).getPredecessorId());
        System.out.println("Successor: " + hostList.get(0).getSuccessorId());
        result.append("HostID\t\t\tAddress\t\tListening port\n");
        result.append("------------------------------------------------------------------\n");
        for(int i=0;i<hostListSize;i++){
            tempHost=hostList.get(i);
            isLocalhost=tempHost.isLocalPeer();
            if(tempHost.isLocalPeer()){
                result.append("(#)");
            }else if(tempHost.getSocket()!=null){
                result.append("(*)");
            }else{
                result.append("   ");
            }
            if(tempHost.getId()==Peer.UNKNOWNPEER){
                result.append("Unknown");
                result.append("\t\t");
            }else if(tempHost.getId()==Peer.PEERWITHOUTID){
                result.append("Not assigned");
                result.append("\t\t");
            }else if(tempHost.getId()==Peer.UNCONNECTED){
                result.append("Not connected");
                result.append("\t");
            }else{
                result.append(tempHost.getId());
                result.append("\t\t\t");
            }

            result.append(tempHost.getIpAddress());
            result.append("\t");

            if(tempHost.getListeningPort()==Peer.UNKNOWN){
                result.append("Unknown");
                
            }else{
                result.append(tempHost.getListeningPort());
            }
            result.append("\n");
        }
        return result.toString();
    }
}
