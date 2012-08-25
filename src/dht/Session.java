/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dht;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.SocketException;
import java.net.SocketTimeoutException;


/**
 *
 * @author sebastiantunstig
 */
public class Session implements Runnable{
    private Peer peer;
    private Host host; //far-end host.
    private HostMap hostMap;
    private int sessionNumber;
    private KeepAlive keepAlive; //Ping:ing object.
    private ThreadPool threadPool;
    private HashTable hashTable;
    
    public Session(Peer peer, Host host, ThreadPool threadPool, HostMap hostMap, HashTable hashTable) throws IOException, SocketException{
        this.peer=peer;
        this.hostMap=hostMap;
        this.host=host;
        this.threadPool=threadPool;
        this.hashTable=hashTable;
        this.sessionNumber=Peer.UNSET;
        host.getSocket().setSoTimeout(Peer.SOCKETTIMEOUTTIME);
        keepAlive = new KeepAlive(Peer.SOCKETTIMEOUTTIME/2, new PrintWriter(host.getSocket().getOutputStream(), true));
        hostMap.setDisconnectAll(false);//If a disconnect was requested from the menu before, it is now over and should not be the case for quitting sessions anymore.
    }
    public synchronized void setSessionNumber(int sessionNumber){
        Peer.notify(Peer.DEBUG, "setting session number to: " + sessionNumber);
        this.sessionNumber=sessionNumber;
    }
    private void closeSession(){
        if(host.isAlive()){
            host.setAlive(false);
        }
        threadPool.delThread(sessionNumber);
        //If this was the last connection we had.
        if(hostMap.size()==0){
            System.out.println("Cleaning up from network data, as this was the last known host we had in our host map.");
            Host localhost=hostMap.getHostList().get(0);
            localhost.setId(Peer.UNCONNECTED);
            localhost.setPredecessorId(Peer.UNCONNECTED);
            localhost.setSuccessorId(Peer.UNCONNECTED);
            hostMap.setVersion(1);
        }else if(hostMap.isDisconnectAll()){
            Peer.notify(Peer.DEBUG, "Disconnection forced by menu (disconnect all)");
            hostMap.setVersion(1);
        }else if(host.getDisconnectFrom()){ //The coordinator made us close this connection.
            //If we disconnected due to changing successor, leave the host in the hostmap.
            Peer.notify(Peer.DEBUG, "Disconnection forced by coordinator");
            host.setDisconnectFrom(false);
            closeSocket();
            host.setSocket(null);
            return;
        }else{//We lost connection to the host. Increment our map version (and broadcast the update if it is not a disconnect for all sessions.)
            Peer.notify(Peer.DEBUG, "Disconnection caused by network problems or closing from other end.");
            //Check if the peer has sent a WILLCLOSE#, meaning the peer is still online in the network but just changing successor/predecessor
            closeSocket();
            //TODO: Check if the host was our successor, in that case we should take over its keys stored in the secondary hashtable.
            if(host.getId()==hostMap.getLocalhost().getSuccessorId()){
                Peer.notify("Lost connection to successor, taking over its data store");
                hashTable.takeOverSuccessorData();
            }
            hostMap.incrementVersion();
            hostMap.removeHost(host);
            hostMap.setRequestStabilize(true);
            hostMap.broadcastVersion();
            return;
        }
        
        
        
        closeSocket();
        hostMap.removeHost(host);
        hostMap.setRequestStabilize(true);
    }
    public void closeSocket(){
        try{
            host.getSocket().close();
        }catch(Exception ex){
            Peer.notify("Got exception when closing socket!");
        }    
    }
    
    @Override
    public void run(){   
        //First, wait for the sessionId to be set.
        while(sessionNumber==Peer.UNSET){
            try{
                Thread.sleep(500);//0.5s
            }catch(Exception e){
            }
        }
        host.setAlive(true);
        BufferedReader br;
        String inputString; //Is set to null when we cannot read from the socket anymore.
        String buffer;
        Thread keepAliveThread = new Thread(keepAlive);
        keepAliveThread.start();
        try {
            br = new BufferedReader(new InputStreamReader(host.getSocket().getInputStream()));
            PrintWriter pw = new PrintWriter(host.getSocket().getOutputStream(), true);
            while(peer.isActive() && !host.getSocket().isClosed() && host.isAlive() && !host.getDisconnectFrom()){
                if((inputString=br.readLine()) != null){
                    if(!inputString.equals("ping")){
                        Peer.notify("<< (" + host.getId() + "): " + inputString);
                        Message message = new Message(inputString, host, hostMap, hashTable);
                        message.handleMessage();
                    }
                }
                if(inputString==null){
                    //Socket is closed from far-end side or we have network problems.
                    Peer.notify("Session #"+sessionNumber+" closing due to lost contact with far-end");
                    host.setAlive(false);
                }
            }if(host.getSocket().isClosed()){
                System.out.println("This socket was closed, and not by me!");
            }
            closeSession();
        }catch(SocketTimeoutException e){
            //Expected when no message is received within Peer.BLOCKREADSOCKETTIME ms.
            Peer.notify("Session #"+sessionNumber+" closing due to lost contact with far-end´");
            closeSession();
        }catch (IOException e) {
            Peer.notify("IOException in Session, run()");
            closeSession();
        }catch(NullPointerException e){
            Peer.notify("Nullpointer Exception in Session, run()");
            closeSession();
        }
    }
}
