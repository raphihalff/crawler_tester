package edu.columbia.cs.dns_server;
import java.io.*;
import java.net.*;

/**
 * Sends a "I am free!" message to LoadBalancer
 * for testing purposes
 */
class UDPClient
{
    public static void main(String args[]) throws Exception
    {
        final String[] COLORS = {"\u001B[31m", "\u001B[32m","\u001B[33m","\u001B[34m","\u001B[35m"};
        final String RESET = "\u001B[0m";

        final DatagramSocket ds2 = new DatagramSocket(20001, InetAddress.getByName("127.0.0.2"));
        final DatagramSocket ds3 = new DatagramSocket(20001, InetAddress.getByName("127.0.0.3"));
        final DatagramSocket ds4 = new DatagramSocket(20001, InetAddress.getByName("127.0.0.4"));
        final DatagramSocket ds5 = new DatagramSocket(20001, InetAddress.getByName("127.0.0.5"));
        final DatagramSocket ds6 = new DatagramSocket(20001, InetAddress.getByName("127.0.0.6"));
               
        final InetAddress IPAddress = InetAddress.getByName("127.0.0.1");
        String sentence = "I am free!";
        final byte[] msg = sentence.getBytes();
        //try {
        //    Thread.sleep(7000);
        //} catch (Exception e){
        //}

        Thread t2 = new Thread(new Runnable() {
           public void run() {
               try {
                   int i = 0;
                   while(i<6){
                       byte[] receiveData = new byte[1024];
                       DatagramPacket s = new DatagramPacket(msg, msg.length, IPAddress, 20001);
                       ds2.send(s);
                       DatagramPacket p= new DatagramPacket(receiveData, receiveData.length);
                       ds2.receive(p);
                       System.out.println("FROM SERVER: " + COLORS[0] + new String(p.getData()) + RESET);
                       i++;
                       Thread.sleep(650);
                   }
               } catch (Exception e) {
                   e.printStackTrace();
               }
               ds2.close();
               return;
           }
        }); 
        Thread t3 = new Thread(new Runnable() {
           public void run() {
               try {
                   int i = 0;
                   while(i<6){
                       byte[] receiveData = new byte[1024];
                       DatagramPacket s = new DatagramPacket(msg, msg.length, IPAddress, 20001);
                       ds3.send(s);
                       DatagramPacket p= new DatagramPacket(receiveData, receiveData.length);
                       ds3.receive(p);
                       System.out.println("FROM SERVER: " + COLORS[1] + new String(p.getData()) + RESET);
                       i++;
                       Thread.sleep(650);
                   }
               } catch (Exception e) {
                   e.printStackTrace();
               }
               ds3.close();
               return;
           }
        });
        Thread t4 = new Thread(new Runnable() {
           public void run() {
               try {
                   int i = 0;
                   while(i<6){
                       byte[] receiveData = new byte[1024];
                       DatagramPacket s = new DatagramPacket(msg, msg.length, IPAddress, 20001);
                       ds4.send(s);
                       DatagramPacket p= new DatagramPacket(receiveData, receiveData.length);
                       ds4.receive(p);
                       System.out.println("FROM SERVER: " + COLORS[2] + new String(p.getData()) + RESET);
                       i++;
                       Thread.sleep(600);
                   }
               } catch (Exception e) {
                   e.printStackTrace();
               }
               ds4.close();
               return;
           }
        });
        Thread t5 = new Thread(new Runnable() {
           public void run() {
               try {
                   int i = 0;
                   while(i<6){
                       byte[] receiveData = new byte[1024];
                       DatagramPacket s = new DatagramPacket(msg, msg.length, IPAddress, 20001);
                       ds5.send(s);
                       DatagramPacket p= new DatagramPacket(receiveData, receiveData.length);
                       ds5.receive(p);
                       System.out.println("FROM SERVER: " + COLORS[3] + new String(p.getData()) + RESET);
                       i++;
                       Thread.sleep(650);
                   }
               } catch (Exception e) {
                   e.printStackTrace();
               }
               ds5.close();
               return;
           }
        });
        Thread t6 = new Thread(new Runnable() {
           public void run() {
               try {
                   int i = 0;
                   while(i<6){
                       byte[] receiveData = new byte[1024];
                       DatagramPacket s = new DatagramPacket(msg, msg.length, IPAddress, 20001);
                       ds6.send(s);
                       DatagramPacket p= new DatagramPacket(receiveData, receiveData.length);
                       ds6.receive(p);
                       System.out.println("FROM SERVER: " + COLORS[4] + new String(p.getData()) + RESET);
                       i++;
                       Thread.sleep(650);
                   }
               } catch (Exception e) {
                   e.printStackTrace();
               }
               ds6.close();
               return;
           }
		
       }); 
       
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        t6.start();

		
        return;
	
    }
	
}
