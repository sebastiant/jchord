/**
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dht;

import java.util.ArrayList;

/**
 *
 * @author sebastiantunstig
 */
public class FingerTable {
    private int size;
    private int arity;
    private int version;
    private HostMap hostMap;
    private ArrayList<Integer> table;
    
    public FingerTable(int arity, HostMap hostMap){
        this.size=logarithm(Peer.SIZEOFNODEIDENTIFIERSPACE, Peer.ARITY); //Logarithm of the size of the identifierspace with the given arity (probably 2) as base for the logarithm.
        this.table=new ArrayList<Integer>();
        this.arity=arity;
        this.hostMap=hostMap;
        this.version=0;
    }
    public ArrayList<Integer> getTable(){
        return table;
    }
    //Given a hashedKey, the return value is the Host, according to the fingerTable that should receive the request.
    public Host lookup(int hashedKey){
        if(table.size()==1){
            System.out.println("Returning localhost.");
            return hostMap.getLocalhost();
        }
        //TODO: IF LOCALHOST, RETURN LOCALHOST?
        
        //iterate through the possible host IDs, counting clockwise from (and including) the key, and check if they're in the table.
        int current=hashedKey;
        for(int i=0;i<Peer.SIZEOFNODEIDENTIFIERSPACE;i++){
            if(current>Peer.SIZEOFNODEIDENTIFIERSPACE){ //"result modulo size"
                current=1;
            }
            if(table.indexOf(current)!=-1){
                return hostMap.getHostFromId(current);
            }
            current++;
        }
        return hostMap.getLocalhost();
    }
    //Updates the linkedList table, according to specifications, containing a finger table of the current hostmap.
    public synchronized void update(){
        int hostMapversion=hostMap.getVersion();
        if(version!=hostMapversion){
            table.clear();
            Peer.notify("Updating fingertable");
            int localId=hostMap.getLocalhost().getId();
            //if we're not connected, set localId to 1 instead of -1. (ugly workaround) TODO: fix!
            if(localId==Peer.UNCONNECTED){
                localId=1;
            }
            int interval=1;
            for(int i=0;i<size;i++){
                interval=interval+(int)(Math.pow(2,i-1));
                while((interval+localId)>Peer.SIZEOFNODEIDENTIFIERSPACE){ //"modulo(size)"
                    interval=interval-Peer.SIZEOFNODEIDENTIFIERSPACE;
                }
                System.out.println("Adding successor for key: "+(interval+localId));
                table.add(i, hostMap.lookup(interval+localId));
            }
            version=hostMapversion;
        }
    }
     
    //computes a logarithm of passed integer with passed base and returns the (rounded if needed) integer as answer.
     static int logarithm(int Of, int base){
            return round(Math.log(Of)/Math.log(base));
    }
    //Rounds a passed float value to its nearest integer.
    private static int round(double value){
        return (int)Math.floor(value+0.5);
    }

    @Override
    public String toString(){
        update();
        int localId=hostMap.getLocalhost().getId();
        StringBuilder sb = new StringBuilder();
        sb.append("size: ");
        sb.append(size);
        sb.append("\nTable contents: ");
        int interval=1;
        for(int i=0;i<size;i++){
            interval=interval+(int)(Math.pow(2,i-1));
            if((interval+localId)>Peer.SIZEOFNODEIDENTIFIERSPACE){
                sb.append(interval+localId-Peer.SIZEOFNODEIDENTIFIERSPACE);
            }else{
                sb.append(interval+localId);
            }
            sb.append(":");
            sb.append(table.get(i));
            sb.append(" ");
        }
        sb.append("\n");
        return sb.toString();
    }
}
