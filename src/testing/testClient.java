/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testing;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 *
 * @author sebastiantunstig
 */
public class testClient {
    public static void main(String[] argv){
        //InetAddress ia = new Inet4Address("localhost");
        System.out.println("Connecting and sending hello");
        try{
            Socket s = new Socket("localhost",1778);
            s.setSoTimeout(50000);
            PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            try {
                
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                System.out.println("Couldn't sleep!");
            }
            
            pw.println("hello, give me portnumber please.");
            pw.flush();
            System.out.println("Receiving port number.");
           
            String str;
            if((str=br.readLine()) != null){
                int newPort;
                try{
                newPort = Integer.parseInt(str);
                }catch(NumberFormatException e){
                    System.out.println("<< "+ str);
                    return;
                }
                System.out.println("Received my special port: " + newPort);
                s.close();
                System.out.println("Creating new connection");
                
                //Initialize the new socket, negotiated on the new port.
                Socket s2 = new Socket("localhost", newPort);
                BufferedReader br2 = new BufferedReader(new InputStreamReader(s2.getInputStream()));
                PrintWriter pw2 = new PrintWriter(s2.getOutputStream(), true);

                //Send 'hi'
                sendMessage(pw2,"hi");
                //Start thread for handling incomming messages.
                Input input = new Input(br2);
                Thread inputThread = new Thread(input);
                inputThread.start();
                
                BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
                String outputString;
                while((outputString=stdin.readLine())!=null && outputString.length()!=0){
                    //todo: send
                    sendMessage(pw2, outputString);
                }
                
                s2.close();
            }
            else{
               System.out.println("Got no answer from other peer.");
            }
            
 
        }catch(UnknownHostException e){
            System.out.println("Super failure uknown host!");
        } catch (IOException e){
            System.out.println("Super failure io!");
        }
        
    }
    static void sendMessage(PrintWriter pw, String msg){
        pw.println(msg);
        System.out.println(">> " +msg);
        pw.flush();
    }
    


}
