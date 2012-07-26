/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dht;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author sebastiantunstig
 */
public class ThreadPool {
    private List<Thread> pool;
    private boolean[] freeThreads;
    private int maxSize;
    
    public ThreadPool(int size){
      pool = new ArrayList<Thread>(size);
      freeThreads = new boolean[size];
      Arrays.fill(freeThreads, Boolean.TRUE);
      maxSize=size;
    }
    public synchronized int size(){
        int result=0;
        for(int i=0;i<maxSize;i++){
            if(freeThreads[i]==false){
                result++;
            }
        }
        return result;
    }
    
    public boolean isFull(){
        return maxSize==size();
    }
    public synchronized int getSessionId(){
        return 1;
    }
    public synchronized int addThread(Thread thread){
        for(int i=0;i<maxSize;i++){
            if(freeThreads[i]){
                try{
                    Thread tempThread=pool.get(i);
                    //If we got here, there is an old thread still there, remove it.
                    try{
                        tempThread=null;
                        pool.remove(i);
                        pool.add(i, thread);
                        freeThreads[i]=false;
                        Peer.notify(Peer.DEBUG, "Adding thread with id: " + i);
                        return i;
                    }catch(Exception e){
                        System.out.println("Couldn't add thread! This will probably result in sincere consequences.");
                    }
                //An exception is expected if there is no object stored on the index.
                }catch(IndexOutOfBoundsException e){
                    try{
                        pool.add(i, thread);
                        freeThreads[i]=false;
                        Peer.notify(Peer.DEBUG, "Adding thread with id: " + i);
                        return i;
                    }catch(Exception e2){
                        System.out.println("Couldn't add thread! This will probably result in sincere consequences.");
                    }
                }
            }
        }
        return -1;
    }
    
    //Marks thread for deletion, meaning the thread will be replaced in the threadPool next time a thread is added.
    public synchronized boolean delThread(int id){
        Peer.notify(Peer.DEBUG, "Deleting thread id: " + id);
        if(!(id>maxSize)){
            if(freeThreads[id]==false){
                freeThreads[id]=true;
                Peer.notify(Peer.DEBUG,"Successfully marked thread id: " + id + " for deletion");
                return true;
            }
        }
        return false;
    }
    
    public void joinAllThreads(){
        for(int i=0;i<maxSize-1;i++){
            if(!freeThreads[i]){
                try {
                    pool.get(i).join();
                } catch (InterruptedException e) {
                    System.err.println("Problem joining threads");
                }
            }
        }
    }
    
}
