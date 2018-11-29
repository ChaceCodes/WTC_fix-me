package com.chgreen.app;

import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
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
        if (Thread.currentThread().isInterrupted()){
            return;
        }
    } 

    private static Attachment getAttach(int ID){
        for (int i = 0; i < routingTable.size(); i++)
        {
            if (routingTable.get(i).ID == ID){
                return (routingTable.get(i));
            }
        }
        return new Attachment();
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
                    ReadWriteHandler rwHandler = new ReadWriteHandler();
                    Attachment newAttach = new Attachment();

                    newAttach.server = attach.server;
                    newAttach.client = client;
                    newAttach.buffer = ByteBuffer.allocate(2048);
                    newAttach.isRead = false;
                    newAttach.clientAddr = clientAddr;
                    newAttach.ID = IDcurrent;
                    newAttach.BoM = 0;

                    IDcurrent++;
                    routingTable.add(newAttach);
                    System.out.println(newAttach.ID + " connected");
                    CharBuffer cbuf = newAttach.buffer.asCharBuffer();
                    cbuf.put("ID:"+ newAttach.ID);
                    cbuf.flip();


                    newAttach.client.write(newAttach.buffer);
                    newAttach.client.read(newAttach.buffer, newAttach, rwHandler);


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
                    //ReadWriteHandler rwHandler = new ReadWriteHandler();
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
                    CharBuffer cbuf = newAttach.buffer.asCharBuffer();
                    cbuf.put("ID:"+ newAttach.ID);
                    cbuf.flip();

                    newAttach.client.write(newAttach.buffer);
                    //newAttach.client.read(newAttach.buffer, newAttach, rwHandler);           
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
        int newCheck = 0;
        int Checksum;
        String tmp;
        String[] splitArray = msg.split("\\|");
        ID = Integer.parseInt(splitArray[0]);
        Checksum = Integer.parseInt(splitArray[7]);
        //System.out.println(Checksum);
        
        for (int i = 0; i < splitArray.length - 1; i++)
        {
            for (int j = 0; j < splitArray[i].length(); j++){
                newCheck += splitArray[i].charAt(j);
            }
            newCheck += '|';
        }
        newCheck -= '|';
        if (Checksum == newCheck){
            return (ID);
        }
        
        return (-1);

    }

    /*
    / ReadWriteHandler(s): 
    */

    private static class ReadWriteHandler implements CompletionHandler<Integer, Attachment>{

        @Override
        public void completed(Integer result, Attachment attach) {
            //Attachment sendAttach = new Attachment();
            if (result == -1){
                try{
                    routingTable.remove(routingTable.indexOf(attach));
                    attach.client.close();
                    System.out.format("Stopped   listening to the   client %s:%d",attach.clientAddr, attach.ID);
                    
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
                int id = processMsg(msg);
                
                //processMsg returns -1 if anything doesn't check out which will result in an invalid checksum
                if (id == -1){
                    System.out.println("Checksum is invalid");
                    attach.isRead = false;
                }
                else {
                    Attachment sendAttach = getAttach(id);

                    if (sendAttach.ID == 0 || (sendAttach.BoM == 0 && attach.BoM == 0) || (sendAttach.BoM == 1 && attach.BoM == 1)){
                        sendAttach = attach;
                    } 
                    System.out.format("Client at  %s:%d  says: %s%n", attach.clientAddr, attach.ID, msg);
                    System.out.format("Client at  %s:%d  says: %s%n", sendAttach.clientAddr, sendAttach.ID, msg);
                    sendAttach.buffer.clear();
                    byte[] data = msg.getBytes(cs);
                    sendAttach.buffer.put(data);
                    sendAttach.buffer.flip();
                    sendAttach.isRead = false;
                    sendAttach.buffer.rewind();
                    //attach.buffer.rewind();
                    //attach.client.write(sendAttach.buffer, attach, this);
                    sendAttach.client.write(sendAttach.buffer, sendAttach, this);
                    
                    //if write to different attach will forwarding be working?
                }
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