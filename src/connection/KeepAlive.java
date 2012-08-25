package connection;

import java.io.PrintWriter;

/**
 *
 * @author sebastiantunstig
 */
public class KeepAlive implements Runnable{
    private int timeout;
    private PrintWriter pw;
    private boolean running;
    
    public KeepAlive(int timeout, PrintWriter pw){
        this.timeout=timeout;
        this.pw=pw;
        this.running=true;
    }
    
    @Override
    public void run(){
        while(running){
            pw.println("ping");
            pw.flush();
            try{
                Thread.sleep(timeout);
            }catch(Exception e){
                System.err.println("trouble!");
            }
        }
    }
    
    public void stop(){
    	running=false;
    }
    
}
