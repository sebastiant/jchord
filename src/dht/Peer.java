/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dht;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Random;

/**
 *
 * @author sebastiantunstig
 */
public class Peer{
    public static final String VERSION = "0.0";
    public static final Boolean DEBUG = true;
    //Configurable constants
    public static final int ARITY = 2; //Arity of fingertable
    public static final int SIZEOFNODEIDENTIFIERSPACE = 64; //Must be a positive integer of the power of 2.
    public static final int LISTENINGPORT = 1778;
    public static final int FIRSTSESSIONPORT = 1890;
    public static final int MAXSESSIONS = 64; //Maximum number of session threads.
    public static final int BLOCKACCEPTSOCKETTIME = 1000;//time to block when accepting connections from a serversocket, in ms.
    public static final int BLOCKREADSOCKETTIME = 5000;//time to block when reading from a socket, in ms.
    public static final int BLOCKRECONNECTTIME = 5000;//Time to wait for new connection when client has been informed of a unique port, in ms.
    public static final int SOCKETTIMEOUTTIME = 5000;//Max time to wait between messages before treating a host as down.
    //Constant constants!
    public static final int UNCONNECTED = -1; //Used as ID when not connected to any other host.
    public static final int PEERWITHOUTID = -2; //Used as ID untill a unique ID is assigned.
    public static final int UNKNOWNPEER = -3; //Used to ID a peer before it has informed its ID.
    public static final int UNKNOWN = -1; //Used as identifier for hosts of which we do not know their listening port
    public static final int UNSET = -1;
    
    private ThreadPool threadPool;
    private ServerSocket serverSocket;
    private HostMap hostMap;
    private HashTable hashTable;
    private int listeningPort;

    private boolean active; //Set to false when the users wishes to exit the program.
    
    
    public Peer() throws IOException{
        active=true;
        threadPool = new ThreadPool(MAXSESSIONS);
        hashTable = new HashTable();
        listeningPort=findFreePort(LISTENINGPORT, FIRSTSESSIONPORT);
        if(listeningPort==-1){
            System.out.println("Maximum amount of local sessions reached. Exiting");
            System.exit(-1);
        }
        serverSocket = new ServerSocket(listeningPort);
        InetAddress localaddr = InetAddress.getLocalHost();
        String localAddress=localaddr.getHostAddress();
        System.out.println("Local IP Address: " + localAddress);
        System.out.println("Using TCP port " + listeningPort + " for incoming connections.");
        serverSocket.setSoTimeout(BLOCKACCEPTSOCKETTIME);
        Host localhost = new Host(UNCONNECTED, localAddress, listeningPort);
        hostMap=new HostMap();
        hostMap.addHost(localhost);
    }
    
    public void startPeer(){
        Socket socket;
        Connection currentConnection;
        while(active){
            try{
                socket = serverSocket.accept();
                if(socket!=null){
                    currentConnection=new Connection(this, threadPool, hostMap, hashTable);
                    ServerSocket newServerSocket = currentConnection.prepareIncomingConnection(socket);
                    socket.close();
                    if(newServerSocket!=null){
                        currentConnection.createIncomingConnection(newServerSocket);
                    }
                }
            }catch(InterruptedException e){
                notify("Problem sleeping thread when waiting for data");
            }catch(SocketTimeoutException e){
                //Expected when no connection is accepted within BLOCKACCEPTSOCKETTIME ms.
            }catch (IOException e) {
                notify("Problem accepting connection and/or negotiating session");
                notify(e.getMessage());
            }
        }
        System.out.println("Shutting down...");
        threadPool.joinAllThreads();
    }
    public HashTable getHashTable() {
        return hashTable;
    }    
    public boolean isActive(){
        return active;
    }
    public void setActive(boolean value){
        active=value;
    } 
    public HostMap getHostMap() {
        return hostMap;
    }
    public ThreadPool getThreadPool() {
        return threadPool;
    }
    public int getListeningPort() {
        return listeningPort;
    }   
    public static int findFreePort(int from, int to){
        if(DEBUG){
            notify("Finding unusued port from "+from+" to "+to);
        }
        ServerSocket testSocket;
        int i=0;
        for(;i<(to-from);i++){
            try{
                testSocket = new ServerSocket(from+i);
                testSocket.close();
                return (from+i);
            }catch(IOException e){
                //Expected to be thrown when we already have a connection using this port.
            }
        }
        return -1;
    }  
    public static void notify(String msg){
        System.out.print("\n(N)" + msg + "\n> ");
    }
    public static void notify(boolean debug, String msg){
        if(debug){
            System.out.print("\n(D)" + msg + "\n> ");
        }
    }
    public static void sendMessage(PrintWriter pw, String receiver, String msg){
        pw.println(msg);
        System.out.print("\n>> ("+ receiver +")" + msg +"\n> ");
        pw.flush();
    }
    public static int getRandomId(){
        Random generator = new Random((int)System.currentTimeMillis());
        return (Math.abs(generator.nextInt())%Peer.SIZEOFNODEIDENTIFIERSPACE)+1;        
    }
    public static void main(String argv[]){
        System.out.println("DHT 0.0 by Sebastian Tunstig, tunstig@kth.se");
        System.out.println("Creating a test peer...");
        
        try{
            Peer peer = new Peer();
            System.out.println("Initializing menu...");
            Menu menu = new Menu(peer);
            System.out.println("Initializing connection coordinator...");
            Stabilizer stabilizer = new Stabilizer(peer, peer.getHostMap(), peer.getThreadPool());
            
            System.out.println("Startup complete.");
            Thread menuThread = new Thread(menu);
            menuThread.start();
            Thread stabilizerThread = new Thread(stabilizer);
            stabilizerThread.start();
            peer.startPeer();
        }catch(IOException e){
            System.err.println("Could not create ServerSocket. Quitting.");
            System.exit(-1);
        }
        
        System.out.println("Everything cleaned up. Exiting.");
        System.exit(0);
    }

}
