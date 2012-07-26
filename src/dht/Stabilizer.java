/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dht;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sebastiantunstig
 */
public class Stabilizer implements Runnable{
    private Peer peer;
    private HostMap hostMap;
    private List<Host> hostList;
    private ThreadPool threadPool;

    
    public Stabilizer(Peer peer, HostMap hostMap, ThreadPool threadPool) {
        this.peer=peer;
        this.hostMap=hostMap;
        this.hostList=hostMap.getHostList();
        this.threadPool=threadPool;
    }
    
    private void disconnectUnwantedHosts(){
        //Iterate through the hostList. If we have a connection to a host that is neither predecessor nor successor, disconnect from it.
        int hostListSize=hostList.size();
        if(hostListSize==1){
            return;
        }
        Host tempHost;
        for(int i=1;i<hostListSize;i++){
            tempHost=hostList.get(i);
            if(tempHost.getSocket()!=null){
                if(tempHost.getId()>0 && !(tempHost.getId()==hostList.get(0).getPredecessorId() || tempHost.getId()==hostList.get(0).getSuccessorId())){
                    Peer.notify("Deciding that we should disconnect from host: " + tempHost.getId());
                    tempHost.setDisconnectFrom(true);
                }
            }
        }
    }

    @Override
    public void run(){
        while(peer.isActive()){
            try{
                Thread.sleep(300);
            }catch(Exception e){}
            if(hostMap.size()>=2 && hostMap.isRequestStabilize()){
                Peer.notify("Stabilizer called");
                hostMap.setRequestStabilize(false);
                //Find the perfect successor. If we do not have it as successor now, do it and inform it about it.
                hostMap.updateDesiredSuccessor();
                if(!hostMap.isDisconnectAll()){//In case we're disconnecting, we shouldn't do anything here.
                    Peer.notify("Checking connection to current successor");
                    connectSuccessor();
                    Peer.notify("Checking connections to hosts in fingerTable");
                    connectFingertable();
                }
            }
        }
    }
    private void connectSuccessor(){
        System.out.println("Desired Successor: "+hostMap.getDesiredSuccessor());
        System.out.println("Current successor: " + hostList.get(0).getSuccessorId());
        if(hostMap.getDesiredSuccessor()!=hostList.get(0).getSuccessorId()){
            Peer.notify("Changing Successor to: "+hostMap.getDesiredSuccessor());
            int newSuccessor=hostMap.getDesiredSuccessor();
            Host tempHost=hostMap.getHostFromId(newSuccessor);
            if(tempHost!=null){
                hostList.get(0).setSuccessorId(newSuccessor);
                if(tempHost.getSocket()==null){ //If we're not connected to the host.
                    Peer.notify("Connecting to new successor (ID: "+newSuccessor+").");
                    Connection connection = new Connection(peer, hostList.get(0).getId(), tempHost.getId(), threadPool, hostMap, peer.getHashTable());
                    try{
                        if(connection.prepareOutgoingConnection()){
                            connection.createOutoingConnection();
                            //Send the host a predecessor-message so it knows that we've changed our successor to him.
                            hostList.get(0).setSuccessorId(newSuccessor);
                        }
                    }catch(UnknownHostException e){
                        Peer.notify("Could not find a host with that IP-address / domain name.");
                    }catch(IOException e){
                        Peer.notify("Could not connect to the host on the given port.");
                    }
                }else{
                    Peer.notify("Already connected to: "+hostMap.getDesiredSuccessor() + " and my ID: "+ hostList.get(0).getId());
                }
                //TODO: Working? clean the current successor-hashtable
                //q: how to handle early predecessor call and separate old data from new. this line wont always be called before new pred received from someoneelse.
                peer.getHashTable().getSuccessorData().clear();
                //Now we're ready to inform the host about us adding him as successor.
                try{
                    tempHost=peer.getHostMap().getHostFromId(newSuccessor);
                    Peer.sendMessage(new PrintWriter(tempHost.getSocket().getOutputStream(), true), "#"+tempHost.getId(), Message.PREDECESSOR+hostList.get(0).getId()+"#"+hostList.get(0).getListeningPort());
                     hostList.get(0).setSuccessorId(newSuccessor);   
                }catch(Exception e){
                    Peer.notify("Couldn't send predecessor info to successor");
                } 
            }else{
                System.out.println("Problemas. TODO");
            }
        }
    }
    private void connectFingertable(){
        peer.getHostMap().getFingerTable().update();
        ArrayList<Integer> fingerHosts=peer.getHostMap().getFingerTable().getTable();
        Host tempHost;
        int localhostId=peer.getHostMap().getLocalhost().getId();
        for(int i=0;i<fingerHosts.size();i++){ //Check all hosts represented in the fingertable, if we're not connected to any of them, connect to it (them).
            tempHost=peer.getHostMap().getHostFromId(fingerHosts.get(i));
            if(tempHost.getSocket()==null && tempHost.getId()!=localhostId){
                Peer.notify("Connecting to FingerTable host (ID: "+tempHost.getId()+").");
                Connection connection = new Connection(peer, peer.getHostMap().getLocalhost().getId(), tempHost.getId(), threadPool, hostMap, peer.getHashTable());
                try{
                    if(connection.prepareOutgoingConnection()){
                        connection.createOutoingConnection();
                    }
                }catch(UnknownHostException e){
                    Peer.notify("Could not find a host with that IP-address / domain name.");
                }catch(IOException e){
                    Peer.notify("Could not connect to the host on the given port.");
                }
                try{
                    tempHost=peer.getHostMap().getHostFromId(tempHost.getId());
                    Peer.sendMessage(new PrintWriter(tempHost.getSocket().getOutputStream(), true), "#"+tempHost.getId(), Message.FINGER+localhostId+"#"+hostList.get(0).getListeningPort());   
                }catch(Exception e){
                    Peer.notify("Couldn't send predecessor info to successor");
                }
            }

        }
    }
    
    //Returns true if both hosts are expected to point fingers to eachother.
    private boolean bidirectionalFinger(int hostId1, int hostId2){
        Peer.notify("Trying to find bidirectionalfinger!");
        int size=FingerTable.logarithm(Peer.SIZEOFNODEIDENTIFIERSPACE, Peer.ARITY);
        boolean result=false;
        int interval=1;
        //Check if hostId1 should connect to hostId2
        for(int i=0;i<size;i++){
            interval=interval+(int)(Math.pow(2,i-1));
            while((interval+hostId1)>Peer.SIZEOFNODEIDENTIFIERSPACE){ //"modulo(size)"
                interval=interval-Peer.SIZEOFNODEIDENTIFIERSPACE;
            }
            if(peer.getHostMap().getFingerTable().lookup(interval+hostId1).getId()==hostId2){
                System.out.println("match on first!");
                int interval2=1;
                //Check if hostId2 should connect to hostId1
                for(int j=0;j<size;j++){
                    interval2=interval2+(int)(Math.pow(2,j-1));
                    while((interval2+hostId2)>Peer.SIZEOFNODEIDENTIFIERSPACE){ //"modulo(size)"
                        interval2=interval2-Peer.SIZEOFNODEIDENTIFIERSPACE;
                    }
                    if(peer.getHostMap().getFingerTable().lookup(interval2+hostId2).getId()==hostId1){
                        System.out.println("match on both!");
                        return true;
                    }

                }
            }
            
        }
        interval=1;
        //Check if hostId2 should connect to hostId2

        return false;
    }
}
