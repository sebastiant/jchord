/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testing;

import java.io.BufferedReader;
import java.io.IOException;

/**
 *
 * @author sebastiantunstig
 */
public class Input implements Runnable{
    private BufferedReader br;
    public Input(BufferedReader br){
        this.br=br;
    }
    public Input(){}

    @Override
    public void run() {
        System.out.println("Input starting up.");
        try {
            String inputString;
            while((inputString=br.readLine())!=null){
                System.out.println("<< " +inputString);
            }
        }catch(IOException e) {
            System.err.println("Problem reading from BufferedReader inside Input.");
        }
    }

}