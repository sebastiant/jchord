/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dht;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.List;


/**
 *
 * @author sebastiantunstig
 */
public class Connection {
    private Peer peer;
    private String inetAddress;
    private int localListeningPort;
    private int farEndListeningPort;
    private int assignedPort;
    private ThreadPool threadPool;
    private HostMap hostMap;
    private HashTable hashTable;
    
    //For connections in an existing network
    private boolean joiningExistingNetwork;
    private int farEndHostId;
    private int localHostId;
    
    //Used for incoming connections
    public Connection(Peer peer, ThreadPool threadPool, HostMap hostMap, HashTable hashTable){
        this.peer=peer;
        this.threadPool=threadPool;
        this.hostMap=hostMap;
        this.hashTable=hashTable;
        this.joiningExistingNetwork=false;
        
    }
    
    //Used for outgoing connections
    public Connection(Peer peer, String inetAddress, int farEndListeningPort, int localListeningPort, ThreadPool threadPool, HostMap hostMap, HashTable hashTable){
        this.peer=peer;
        this.inetAddress=inetAddress;
        this.farEndListeningPort=farEndListeningPort;
        this.localListeningPort=localListeningPort;
        this.threadPool=threadPool;
        this.hostMap=hostMap;
        this.hashTable=hashTable;
        this.joiningExistingNetwork=false;
    }
    //Used for outgoing connections to an established network
    public Connection(Peer peer, int localHostId, int farEndHostId, ThreadPool threadPool, HostMap hostMap, HashTable hashTable){
        this.peer=peer;
        this.joiningExistingNetwork=true;
        this.farEndHostId=farEndHostId;
        List<Host> hostList = hostMap.getHostList();
        Host tempHost;
        for(int i=0;i<hostList.size();i++){
            tempHost=hostList.get(i);
            if(tempHost.getId()==farEndHostId){
                this.inetAddress=tempHost.getIpAddress();
                this.farEndListeningPort=tempHost.getListeningPort();
            }
        }
        this.localHostId=localHostId;
        this.threadPool=threadPool;
        this.hostMap=hostMap;   
        this.hashTable=hashTable;
    }
    
    public ServerSocket prepareIncomingConnection(Socket socket) throws IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
        socket.setSoTimeout(Peer.BLOCKREADSOCKETTIME);
        
        Peer.notify("Peer from ip: " + socket.getInetAddress().getHostAddress() + " has connected.");
        
        if(threadPool.isFull()){
            Peer.sendMessage(pw, socket.getInetAddress().getCanonicalHostName(), "No empty sessions. Closing connection");
            return null;
        }
        
        String str;
        if((str=br.readLine()) != null){
            int freePort=Peer.findFreePort(Peer.FIRSTSESSIONPORT,Peer.FIRSTSESSIONPORT+Peer.MAXSESSIONS);
            if(freePort==-1){
                Peer.notify("Found no port!");
                Peer.sendMessage(pw, socket.getInetAddress().getCanonicalHostName(), "No empty sessions. Closing connection");
                return null;
            }else{
                Peer.notify("Found port to use: " + freePort);
            }
            Peer.sendMessage(pw, socket.getInetAddress().getCanonicalHostName(), String.valueOf(freePort));
            ServerSocket newServerSocket = new ServerSocket(freePort);
            return newServerSocket;
            
        }
        else{
            Peer.notify("No handshake received.");
            return null;
        }
        
    }   
    public void createIncomingConnection(ServerSocket serverSocket) throws IOException, SocketTimeoutException, InterruptedException{
        Peer.notify("Waiting for peer to connect again!");
        Socket socket=new Socket();
        socket.setSoTimeout(Peer.BLOCKRECONNECTTIME);
        socket = serverSocket.accept();
         //If the connection is made from localhost, change the peer's IP from the loopback-address to the network address.
        String farEndIp=socket.getInetAddress().getHostAddress();
        if(farEndIp.equals("127.0.0.1")){
            InetAddress localaddr = InetAddress.getLocalHost();
            farEndIp=localaddr.getHostAddress();
        }
        Host host = new Host(Peer.PEERWITHOUTID, farEndIp, Peer.UNKNOWN, socket);
        hostMap.addHost(host);
        //Start thread for handling the session.
        Session session = new Session(peer, host, threadPool, hostMap, hashTable);
        Thread sessionThread = new Thread(session);
        sessionThread.start();
        session.setSessionNumber(threadPool.addThread(sessionThread));
    }
    public boolean prepareOutgoingConnection() throws UnknownHostException, IOException{
        System.out.println("Connecting");
        Socket socket = new Socket(inetAddress,farEndListeningPort);
        socket.setSoTimeout(Peer.BLOCKREADSOCKETTIME);
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        
        Peer.sendMessage(pw, socket.getInetAddress().getCanonicalHostName(), "<anytihng>!");
        Peer.notify("Waiting for portnumber");
        
        String str;
        if((str=br.readLine()) != null){
            try{
            assignedPort = Integer.parseInt(str);
            }catch(NumberFormatException e){
                Peer.notify("Bad format of expected port number. Original message: "+str);
                return false;
            }
            Peer.notify("Received my special port: " + assignedPort);
            socket.close();
            return true;
        }
        else{
           System.out.println("Got no answer from other peer.");
        }
        return false;
    }
    public void createOutoingConnection() throws UnknownHostException, IOException{
        
        System.out.println("Creating new connection");
        //Initialize the new socket, negotiated on the new port.
        Socket socket = new Socket(inetAddress, assignedPort);
        PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
        Host host;
        //Send 'hi'
        if(!joiningExistingNetwork){
            Peer.sendMessage(pw, socket.getInetAddress().getCanonicalHostName(), Message.HELOMSG+localListeningPort);
            farEndHostId=Peer.UNKNOWNPEER;
            
        }else if(hostMap.getHostFromId(farEndHostId)!=null){ //If connecting to a peer within the network.
            //Remove the old entry for this host in the Hostmap, and set this to connected.
            System.out.println("Removing old host from hostMap: " +farEndHostId);
            hostMap.removeHost(hostMap.getHostFromId(farEndHostId));
        }
        //If the connection is made to localhost, change the peer's IP from the loopback-address to the network address.
        String farEndIp=socket.getInetAddress().getHostAddress();
        if(farEndIp.equals("127.0.0.1")){
            InetAddress localaddr = InetAddress.getLocalHost();
            farEndIp=localaddr.getHostAddress();
        }
        host = new Host(farEndHostId, farEndIp, farEndListeningPort, socket);
        hostMap.addHost(host);
        //Start thread for handling the session.
        Session session = new Session(peer, host, threadPool, hostMap, hashTable);
        Thread sessionThread = new Thread(session);
        sessionThread.start();
        session.setSessionNumber(threadPool.addThread(sessionThread));
    }
    
}
