/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testing;

import dht.ThreadPool;
/**
 *
 * @author sebastiantunstig
 */
public class TestThread implements Runnable{
    String name;
    public TestThread(String name){
        this.name=name;
    }
    
    @Override
    public void run(){
    }
    public static void main(String argv[]){
        System.out.println("Starting tests");
        System.out.println("---");
        System.out.println("Creating a threadpool of size 10.");
        ThreadPool test1 = new ThreadPool(10);
        System.out.println("Creating 10 threads");
        Thread t0 = new Thread(new TestThread("asd1"));
        Thread t1 = new Thread(new TestThread("asd2"));
        Thread t2 = new Thread(new TestThread("asd3"));
        Thread t3 = new Thread(new TestThread("asd4"));
        Thread t4 = new Thread(new TestThread("asd5"));
        Thread t5 = new Thread(new TestThread("asd6"));
        Thread t6 = new Thread(new TestThread("asd7"));
        Thread t7 = new Thread(new TestThread("asd8"));
        Thread t8 = new Thread(new TestThread("asd9"));
        Thread t9 = new Thread(new TestThread("asd10"));
        
        System.out.println("Adding them to pool, testing their id:s are correct");
        if(0==test1.addThread(t0)){
            System.out.println("ok");
        }else System.out.println("false");
        if(1==test1.addThread(t1)){
            System.out.println("ok");
        }else System.out.println("false");
        if(2==test1.addThread(t2)){
            System.out.println("ok");
        }else System.out.println("false");
        if(3==test1.addThread(t3)){
            System.out.println("ok");
        }else System.out.println("false");
        if(4==test1.addThread(t4)){
            System.out.println("ok");
        }else System.out.println("false");
        if(5==test1.addThread(t5)){
            System.out.println("ok");
        }else System.out.println("false");
        if(6==test1.addThread(t6)){
            System.out.println("ok");
        }else System.out.println("false");
        if(7==test1.addThread(t7)){
          System.out.println("ok");
        }else System.out.println("false");
        if(8==test1.addThread(t8)){
            System.out.println("ok");
        }else System.out.println("false");
        if(9==test1.addThread(t9)){
            System.out.println("ok");
        }else System.out.println("false");
        
        System.out.print("Trying to add an eleventh thread to the pool (expect failure)\t");
        Thread t11 = new Thread();
        if(-1==test1.addThread(t11)){
            System.out.println("ok");
        }else System.out.println("false");
        
        System.out.print("Trying to delete invalid threadid 12 (expect failure)\t");
        if(false==test1.delThread(12)){
            System.out.println("ok");
        }else System.out.println("false");
        
        System.out.print("Checking if pool is full\t");
        if(true==test1.isFull()){
            System.out.println("ok");
        }else System.out.println("false");
        
        System.out.print("Trying to delete threadid 0\t");
        if(true==test1.delThread(0)){
            System.out.println("ok");
        }else System.out.println("false");
        
        System.out.print("Trying to delete threadid 3\t");
        if(true==test1.delThread(3)){
            System.out.println("ok");
        }else System.out.println("false");
        
        
        System.out.print("Trying to delete threadid 5\t");
        if(true==test1.delThread(5)){
            System.out.println("ok");
        }else System.out.println("false");
        
        System.out.print("Checking if pool is not full\t");
        if(false==test1.isFull()){
            System.out.println("ok");
        }else System.out.println("false");
        
        System.out.print("Trying to add threadid 6 to 0's old spot.\t");
        if(0==test1.addThread(t6)){
            System.out.println("ok");
        }else System.out.println("false");
        
        System.out.print("Trying to add threadid 6 to 3's old spot.\t");
        if(3==test1.addThread(t6)){
            System.out.println("ok");
        }else System.out.println("false");
        
        System.out.print("Trying to add threadid 6 to 5's old spot.\t");
        if(5==test1.addThread(t6)){
            System.out.println("ok");
        }else System.out.println("false");
        
        
        System.out.print("Checking if pool is full\t");
        if(true==test1.isFull()){
            System.out.println("ok");
        }else System.out.println("false");
        
        System.out.print("Joining all threads\t");
        test1.joinAllThreads();
        System.out.println("Done");   
    }
}
