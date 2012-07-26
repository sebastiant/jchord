/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dht;

import java.net.Socket;

/**
 *
 * @author sebastiantunstig
 */
public class Host {
    private boolean localPeer; //Set to true if the Host represents our own peer.
    private boolean disconnectFrom; //Changed by the ConnectionCoordinator in case of predecessor/successor change.
    private boolean alive; //Is set to false if a session is to be closed.
    private int id;
    private int listeningPort;
    private String ipAddress;
    private Socket socket;
    private int predecessorId;
    private int successorId;
    
    //For the local peer
    public Host(int id, String ipAddress, int listeningPort){
        localPeer=true;
        this.id = id;
        this.ipAddress=ipAddress;
        this.listeningPort = listeningPort;
        this.predecessorId=Peer.UNKNOWN;
        this.successorId=Peer.UNKNOWN;
        socket=null;
    }
    
    //For far-end peers.
    public Host(int id, String ipAddress, int listeningPort, Socket socket){
        this.disconnectFrom=false;
        this.id = id;
        this.ipAddress = ipAddress;
        this.listeningPort = listeningPort;
        this.socket = socket;
        localPeer=false;
    }

    public boolean isAlive() {
        return alive;
    }
    public void setAlive(boolean alive) {
        this.alive = alive;
    }
    public boolean isLocalPeer() {
        return localPeer;
    }
    public boolean getDisconnectFrom() {
        return disconnectFrom;
    }
    public synchronized void setDisconnectFrom(boolean disconnectFrom) {
        this.disconnectFrom = disconnectFrom;
    }  
    public int getId() {
        return id;
    }
    public synchronized void setId(int id) {
        this.id = id;
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public synchronized void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    public int getListeningPort() {
        return listeningPort;
    }
    public synchronized void setListeningPort(int listeningPort) {
        this.listeningPort = listeningPort;
    } 
    public Socket getSocket() {
        return socket;
    }   
    public synchronized void setSocket(Socket socket){
        this.socket=socket;
    }  
    public int getPredecessorId() {
        return predecessorId;
    }
    public synchronized void setPredecessorId(int predecessorId) {
        this.predecessorId = predecessorId;
    }
    public int getSuccessorId() {
        return successorId;
    }
    public synchronized void setSuccessorId(int successorId) {
        this.successorId = successorId;
    }
}
