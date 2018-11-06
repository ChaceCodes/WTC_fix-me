package com.chgreen.app;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.ArrayList;



public class Router 
{
    private static ArrayList<Attachment> routingTable;
    private static int IDcurrent;
    //private static int index;
    public static void main( String[] args ) throws Exception
    {
        IDcurrent = 1000;
        routingTable = new ArrayList<Attachment>();
       //index = 1;
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
        //attachB.ID = IDcurrent++;
        serverMarket.accept(attachM, new ConnectionHandlerM());
        //attachM.ID = IDcurrent++;

        // System.out.println(attachB.ID);
        // System.out.println(attachM.ID); 
        Thread.currentThread().join();
    } 

    private static class Attachment {
        AsynchronousServerSocketChannel server;
        AsynchronousSocketChannel client;
        ByteBuffer buffer;
        SocketAddress clientAddr;
        boolean isRead;
        int ID;
    }

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

    private static class ReadWriteHandlerB implements CompletionHandler<Integer, Attachment>{

        @Override
        public void completed(Integer result, Attachment attach) {
            if (result == -1){
                try{
                    routingTable.remove(routingTable.indexOf(attach));
                    attach.client.close();
                    System.out.format("Stopped   listening to the   client %s%n",attach.clientAddr);
                    
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
                System.out.format("Client at  %s  says: %s%n", attach.clientAddr, msg);
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
    
    private static class ReadWriteHandlerM implements CompletionHandler<Integer, Attachment>{

            @Override
            public void completed(Integer result, Attachment attach) {
                if (result == -1){
                    try{
                        System.out.println("TRY -1 test");
                        attach.client.close();
                        System.out.format("Stopped   listening to the   client %s%n",attach.clientAddr);
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                    return;
                }
        
                if (attach.isRead){
                    System.out.println("ISREAD TEST");
                    attach.buffer.flip();
                    int limits = attach.buffer.limit();
                    byte[] bytes = new byte[limits];
                    attach.buffer.get(bytes, 0, limits);
                    Charset cs = Charset.forName("UTF-8");
                    String msg = new String(bytes, cs);
                    System.out.format("Client at  %s  says: %s%n", attach.clientAddr, msg);
                    //attach.isRead = false;
                    attach.buffer.rewind();
                }
                else {
                    attach.client.write(attach.buffer, attach, this);
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