package com.chgreen.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
//import java.util.Scanner;
import java.util.concurrent.Future;
import com.chgreen.app.PromptUser;

public class Broker 
{
    
    public static void main( String[] args ) throws Exception
    {
        //Scanner scanner = new Scanner(System.in);
        AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
        SocketAddress serverAddr = new InetSocketAddress("localhost", 5000);
        Future<Void> result = channel.connect(serverAddr);
        result.get();
        System.out.format("Connected to server at %s%n", serverAddr);
        Attachment attach = new Attachment();
        attach.channel = channel;
        attach.buffer = ByteBuffer.allocate(2048);
        attach.isRead = true;
        attach.mainThread = Thread.currentThread();
    while (true){
            Charset cs = Charset.forName("UTF-8");
            String msg = PromptUser.mainPrompt();
            byte[] data = msg.getBytes(cs);
            attach.buffer.put(data);
            attach.buffer.flip();
            
        
            ReadWriteHandler readWriteHandler = new ReadWriteHandler();
            channel.write(attach.buffer, attach, readWriteHandler);
            attach.mainThread.join();         
        }
    }
}

class Attachment {
    AsynchronousSocketChannel channel;
    ByteBuffer buffer;
    Thread mainThread;
    boolean isRead;
  }


  class ReadWriteHandler implements CompletionHandler<Integer, Attachment> {
    @Override
    public void completed(Integer result, Attachment attach) {
      if (attach.isRead) {
        attach.buffer.flip();
        Charset cs = Charset.forName("UTF-8");
        int limits = attach.buffer.limit();
        byte bytes[] = new byte[limits];
        attach.buffer.get(bytes, 0, limits);
        String msg = new String(bytes, cs);
        System.out.format("Server Responded: "+ msg);
        try {
          msg = this.promptUser();
        } catch (Exception e) {
          e.printStackTrace();
        }
        if (msg.equalsIgnoreCase("bye")) {
          attach.mainThread.interrupt();
          return;
        }
        attach.buffer.clear();
        byte[] data = msg.getBytes(cs);
        attach.buffer.put(data);
        attach.buffer.flip();
        attach.isRead = false; // It is a write
        attach.channel.write(attach.buffer, attach, this);
      }else {
        attach.isRead = true;
        attach.buffer.clear();
        attach.channel.read(attach.buffer, attach, this);
      }
    }
    @Override
    public void failed(Throwable e, Attachment attach) {
      e.printStackTrace();
    }
    private String promptUser() throws Exception{
      System.out.print("\nPlease enter a  message  (Bye  to quit):");
      BufferedReader consoleReader = new BufferedReader(
          new InputStreamReader(System.in));
      String msg = consoleReader.readLine();
      return msg;
    }
  }