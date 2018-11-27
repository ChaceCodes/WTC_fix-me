package com.chgreen.app;

import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;



public class Router 
{
    /*Private variables  used throughout class
     /routingTable: list of attachments to locat ID and address for message forwarding
     /IDcurrent: Keeps track of connections to ensure a unique ID is given to each attachment
    */
    private static ArrayList<Attachment> routingTable;
    private static int IDcurrent;

    /*
    /
    */
    public static void main( String[] args ) throws Exception
    {
        IDcurrent = 1000;
        routingTable = new ArrayList<Attachment>();
        AsynchronousServerSocketChannel serverBroker = AsynchronousServerSocketChannel.open();
        AsynchronousServerSocketChannel serverMarket = AsynchronousServerSocketChannel.open();
        String host = "localhost";
        int portBroker = 5000;
        int portMarket = 5001;

        InetSocketAddress sAddrBroker = new InetSocketAddress(host, portBroker);     
        InetSocketAddress sAddrMarket = new InetSocketAddress(host, portMarket);     
        serverBroker.bind(sAddrBroker);      
        serverMarket.bind(sAddrMarket); 
 
        
        System.out.format("Server is listening for Broker at %s%n", sAddrBroker);
        System.out.format("Server is listening for Market at %s%n", sAddrMarket);
        Attachment attachB = new Attachment();
        Attachment attachM = new Attachment();
        attachB.server = serverBroker;
        attachM.server = serverMarket;
        serverBroker.accept(attachB, new ConnectionHandlerB());
        serverMarket.accept(attachM, new ConnectionHandlerM());
        Thread.currentThread().join();
    } 


    /*
    / Attachment struct to store data of new attachments
    */

    private static class Attachment {
        AsynchronousServerSocketChannel server;
        AsynchronousSocketChannel client;
        ByteBuffer buffer;
        SocketAddress clientAddr;
        boolean isRead;
        //BoM 1 if Market 0 if Broker
        int BoM;
        int ID;
    }

    /*
    / ConnectionHandler(s): Take on new connections and assign nescessary data to Attachment class and routingTable
    */

    private static class ConnectionHandlerB implements CompletionHandler<AsynchronousSocketChannel, Attachment>{

        @Override
        public void completed(AsynchronousSocketChannel client, Attachment attach) {
                try{
                    SocketAddress clientAddr = client.getRemoteAddress();
                    System.out.format("Accepted a  connection from  %s%n", clientAddr);
                    attach.server.accept(attach, this);
                    ReadWriteHandlerB rwHandler = new ReadWriteHandlerB();
                    Attachment newAttach = new Attachment();
                    newAttach.server = attach.server;
                    newAttach.client = client;
                    newAttach.buffer = ByteBuffer.allocate(2048);
                    newAttach.isRead = true;
                    newAttach.clientAddr = clientAddr;
                    newAttach.ID = IDcurrent;
                    newAttach.BoM = 0;
                    IDcurrent++;
                    routingTable.add(newAttach);
                    System.out.println(newAttach.ID + " connected");
                    client.read(newAttach.buffer, newAttach, rwHandler);
            }
            catch(IOException e){
                e.printStackTrace();
            }

        }

        @Override
        public void failed(Throwable exc, Attachment attachment) {
                System.out.println("Failed to accept a connection.");
                exc.printStackTrace();
        }

    }


    private static class ConnectionHandlerM implements CompletionHandler<AsynchronousSocketChannel, Attachment>{

        @Override
        public void completed(AsynchronousSocketChannel client, Attachment attach) {
            //int idCurrent = 1000;
                try{
                    SocketAddress clientAddr = client.getRemoteAddress();
                    System.out.format("Accepted a  connection from  %s%n", clientAddr);
                    attach.server.accept(attach, this);
                    ReadWriteHandlerM rwHandler = new ReadWriteHandlerM();
                    Attachment newAttach = new Attachment();
                    newAttach.server = attach.server;
                    newAttach.client = client;
                    newAttach.buffer = ByteBuffer.allocate(2048);
                    newAttach.isRead = true;
                    newAttach.clientAddr = clientAddr;
                    newAttach.ID = IDcurrent;
                    newAttach.BoM = 1;
                    IDcurrent++;
                    routingTable.add(newAttach);
                    System.out.println(newAttach.ID + " connected");
                   // System.out.println(routingTable.toString());
                    client.read(newAttach.buffer, newAttach, rwHandler);            
            }
            catch(IOException e){
                e.printStackTrace();
            }

        }

        @Override
        public void failed(Throwable exc, Attachment attachment) {
                System.out.println("Failed to accept a connection.");
                exc.printStackTrace();
        }

    }

    /*
    / processMsg(String msg): returns: -1 if invalid else ID in FIX_Message as an int

    */

    private static int processMsg(String msg){
        int ID;
        ID = 0;
        String tmp;
        List<Character> buff = new ArrayList<Character>();
        List<Character> buffCheck = new ArrayList<Character>();
        int i = 0;
        int checkSum = 0;
        char c;
        int checkCheck = 0;

        c = msg.charAt(i);
        i++;


        while ((c != '|')){
            buff.add(c);
            
            c = msg.charAt(i);
            i++;
        }

		StringBuilder sb = new StringBuilder();
		for (Character ch: buff) {
			sb.append(ch);
		}
		tmp = sb.toString();
        ID = Integer.parseInt(tmp);
        System.out.println(ID);

        buff.add(c);  
        c = msg.charAt(i);
        i++;
        
        
        for (int j = 0; j < 4; j++) {
            while ((c != '|')){
                buff.add(c);                
                c = msg.charAt(i);
                i++;
            }
            buff.add(c);  
            c = msg.charAt(i);
            i++;
        }


        while ((i < msg.length())){
            buffCheck.add(c);
            c = msg.charAt(i);
            i++;
        }

        buffCheck.add(c);
        System.out.println(buffCheck.toString());
        
        
        StringBuilder sbCheck = new StringBuilder();
		for (Character ch: buffCheck) {
		sbCheck.append(ch);
		}
		tmp = sbCheck.toString();
        checkSum = Integer.parseInt(tmp);
        

        System.out.println(checkSum);

        for (int k = 0; k < msg.length() - (buffCheck.size() + 1); k++){
           // System.out.println("Checkc = " + checkCheck);
            checkCheck += msg.charAt(k);
        }

        if (checkCheck == checkSum){
            //System.out.println(checkCheck + " " + checkSum);
            return ID;
        }
        else
        {
            System.out.println("invalid :" + checkCheck);
            return(-1);
        }
    }

    /*
    / ReadWriteHandler(s): 
    */

    private static class ReadWriteHandlerB implements CompletionHandler<Integer, Attachment>{

        @Override
        public void completed(Integer result, Attachment attach) {
            //Attachment sendAttach = new Attachment();
            if (result == -1){
                try{
                    routingTable.remove(routingTable.indexOf(attach));
                    attach.client.close();
                    System.out.format("Stopped   listening to the   client %s%n%d",attach.clientAddr, attach.ID);
                    
                }
                catch(IOException e){
                    e.printStackTrace();
                }
                return;
            }

            if (attach.isRead){
                attach.buffer.flip();
                int limits = attach.buffer.limit();
                byte[] bytes = new byte[limits];
                attach.buffer.get(bytes, 0, limits);
                Charset cs = Charset.forName("UTF-8");
                String msg = new String(bytes, cs);
                processMsg(msg);

                System.out.format("Client at  %s:%d  says: %s%n", attach.clientAddr, attach.ID, msg);
                // currentattach = findAttach{}
                attach.isRead = false;
                attach.buffer.rewind();
                attach.client.write(attach.buffer, attach, this);
                //if write to different attach will forwarding be working?
            }
            else {
                attach.isRead = true;
                attach.buffer.clear();
                attach.client.read(attach.buffer, attach, this);
            }

        }

        @Override
        public void failed(Throwable exc, Attachment attach) {
                exc.printStackTrace();
        }
    }
    
    private static class ReadWriteHandlerM implements CompletionHandler<Integer, Attachment>{

            @Override
            public void completed(Integer result, Attachment attach) {
                Attachment sendAttach = new Attachment();
                if (result == -1){
                    try{
                        routingTable.remove(routingTable.indexOf(attach));
                        attach.client.close();
                        System.out.format("Stopped   listening to the   client %s%n%d",attach.clientAddr, attach.ID);
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                    return;
                }
        
                if (attach.isRead){
                    attach.buffer.flip();
                    int limits = attach.buffer.limit();
                    byte[] bytes = new byte[limits];
                    attach.buffer.get(bytes, 0, limits);
                    Charset cs = Charset.forName("UTF-8");
                    String msg = new String(bytes, cs);
                    processMsg(msg);
                    
                    System.out.format("Client at  %s:%d  says: %s%n", attach.clientAddr, attach.ID, msg);
                    attach.isRead = false;
                    attach.buffer.rewind();
                    attach.client.write(attach.buffer, attach, this);
                }
                else {
                    attach.isRead = true;
                    attach.buffer.clear();
                    attach.client.read(attach.buffer, attach, this);
                }
        
            }
        
            @Override
            public void failed(Throwable exc, Attachment attach) {
                    exc.printStackTrace();
            }

    }

}