package com.chgreen.app;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;


public class Router 
{
    
    public static void main( String[] args ) throws Exception
    {

        AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel
        .open();
        String host = "localhost";
        int portBroker = 5000;
        int portMarket = 5001;
        InetSocketAddress sAddrBroker = new InetSocketAddress(host, portBroker);       
        server.bind(sAddrBroker);      
        System.out.format("Server is listening for Broker at %s%n", sAddrBroker);
        
        Attachment attachB = new Attachment();
        System.out.println("This");
        attachB.server = server;
        System.out.println("still");
        server.accept(attachB, new ConnectionHandler());
        Thread.currentThread().join();
        System.out.println("works");

        InetSocketAddress sAddrMarket = new InetSocketAddress(host, portMarket);   

        server.bind(sAddrMarket);     
        System.out.println("This");
        System.out.format("Server is listening for Market at %s%n", sAddrMarket);

        Attachment attachM = new Attachment();
        attachM.server = server;
        server.accept(attachM, new ConnectionHandler());
        //Thread.currentThread().join();
        
        
        
        
        
        
        /*int totalBrokers = 0;
        int totalMarkets = 0;
        ServerSocket serverSocketBroker = new ServerSocket(5000);
        Socket socketBroker = serverSocketBroker.accept();
        System.out.println( "when does this  happen?" );
        InputStreamReader IRBroker = new InputStreamReader(socketBroker.getInputStream());
        BufferedReader BRBroker = new BufferedReader(IRBroker);

        while(true){
            String message = BRBroker.readLine();
            System.out.println(message);
            if (message.equalsIgnoreCase("exit")){
                serverSocketBroker.close();
                return;
            }

            if (message != null)
            {
                PrintStream PSBroker = new PrintStream(socketBroker.getOutputStream());
                PSBroker.println("Message received!");
            }
        }*/
    }

    private int validateCheckSum(){

        return(0);
    }

    private int generateID(){

        return(0);
    } 
}

class Attachment {
    AsynchronousServerSocketChannel server;
    AsynchronousSocketChannel client;
    ByteBuffer buffer;
    SocketAddress clientAddr;
    boolean isRead;
    int ID;
}

class ConnectionHandler implements CompletionHandler<AsynchronousSocketChannel, Attachment>{

    @Override
    public void completed(AsynchronousSocketChannel client, Attachment attach) {
        int idCurrent = 1000;
            try{
                SocketAddress clientAddr = client.getRemoteAddress();
                System.out.format("Accepted a  connection from  %s%n", clientAddr);
                attach.server.accept(attach, this);
                ReadWriteHandler rwHandler = new ReadWriteHandler();
                Attachment newAttach = new Attachment();
                newAttach.server = attach.server;
                newAttach.client = client;
                newAttach.buffer = ByteBuffer.allocate(2048);
                newAttach.isRead = true;
                newAttach.clientAddr = clientAddr;
                newAttach.ID = idCurrent;
                idCurrent++;
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

class ReadWriteHandler implements CompletionHandler<Integer, Attachment>{

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