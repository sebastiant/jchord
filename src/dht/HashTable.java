/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dht;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 *
 * @author sebastiantunstig
 */
public class HashTable {
    /*
     * Returns the successor of a key, given a hostmap.
     */
    private HashMap<Integer, String> data; //Local datastore
    private HashMap<Integer, String> successorData; //Successor's datastore to be taken over if we loose contact with successor.
    public HashTable(){
        data=new HashMap<Integer, String>();
        successorData=new HashMap<Integer, String>();
    }

    public int size(){
        return data.size();
    }
    public String getData(int identifier){
        return data.get(identifier);
    }
    public synchronized void addData(int key, String load) {
        data.put(new Integer(key), load);
        Peer.notify("Added Mapping for hash: " + key + " in local hashtable");
    }
    public synchronized void delData(int key){
        if(data.remove(key)==null){
            Peer.notify("Tried to remove unexisting data from HashTable");
        }else{
            Peer.notify("Removed mapping for key: "+ key +" in local hashtable");
        }
    }
    
    public synchronized HashMap<Integer,String> getSuccessorData(){
        return successorData;
    }
    public synchronized void addSuccessorData(int key, String load){}
    
    //Returns a HashMap containing all local data stored with a key from (and including) fromKey to (and including) toKey counting clockwise.
    public synchronized HashMap<Integer,String> getPredecessorData(int fromKey, int toKey){
        System.out.println("Retreiving predescessor data");
        HashMap<Integer,String> result = new HashMap<Integer,String>();
        int elements;
        
        //We're working in a ring, which implies that if toKey is less than fromKey, 0 is between the ids in the ring.
        if(fromKey>=toKey){
            elements=Peer.SIZEOFNODEIDENTIFIERSPACE-fromKey+toKey;
        }else
            elements=toKey-fromKey;
        
        int index=fromKey;
        System.out.println("Starting iteration of "+ elements + " elements");
        for(int i=0;i<elements;i++){
            if(index==Peer.SIZEOFNODEIDENTIFIERSPACE+1)
                index=1; //"modulo <size>"
            //System.out.print("i:"+index);
            if(data.get(index)!=null){
                //System.out.println("match!");
                String load=data.remove(index);
                if(load!=null){
                    //System.out.println("Found data on key: "+index+" containing: "+load);
                    result.put(index, load);
                }
            }
            index+=1;   
        }
        return result;
    }
    //TODO: check.
    public synchronized void takeOverSuccessorData(){
        successorData.putAll(data);
    }
    public static int generateKeyFromSHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException  { 
        Peer.notify("Calculating hashvalue for text: " + text);
        MessageDigest md;
        md = MessageDigest.getInstance("SHA-1");
        byte[] sha1hash = new byte[40];
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        return (byteArrayToInt(md.digest(), 0) & Peer.SIZEOFNODEIDENTIFIERSPACE-1);
    }
    
    /*
     * Method converts a byteArray to an integer representation gotten from its binary data.
     * Code gotten from: http://snippets.dzone.com/posts/show/94
     * 
     */
    public static int byteArrayToInt(byte[] b, int offset) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }
}
