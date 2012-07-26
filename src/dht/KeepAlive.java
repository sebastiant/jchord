/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dht;

import java.io.PrintWriter;

/**
 *
 * @author sebastiantunstig
 */
public class KeepAlive implements Runnable{
    private int timeout;
    private PrintWriter pw;
    public KeepAlive(int timeout, PrintWriter pw){
        this.timeout=timeout;
        this.pw=pw;
    }
    
    @Override
    public void run(){
        while(true){
            pw.println("ping");
            pw.flush();
            try{
                Thread.sleep(timeout);
            }catch(Exception e){
                Peer.notify("Problem sleeping in KeepAlive-thread!");
            }
        }
    }
    
}
