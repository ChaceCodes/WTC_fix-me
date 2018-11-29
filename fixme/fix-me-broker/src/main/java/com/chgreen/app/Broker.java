package com.chgreen.app;

// import java.io.BufferedReader;
// import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.concurrent.Future;
import com.chgreen.app.PromptUser;

public class Broker 
{
    public static void main( String[] args ) throws Exception
    {
      try{
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
        ReadWriteHandler readWriteHandler = new ReadWriteHandler();
        channel.read(attach.buffer, attach, readWriteHandler);
        attach.mainThread.join();
        if (attach.mainThread.isInterrupted()){
          return;
        }
      }catch(Exception e){
        System.out.println("Router Unavailable");
      }         
    }
}

class Attachment {
    int id;
    AsynchronousSocketChannel channel;
    ByteBuffer buffer;
    Thread mainThread;
    boolean isRead;
  }



  class ReadWriteHandler implements CompletionHandler<Integer, Attachment>{
    @Override
    public void completed(Integer result, Attachment attach){
      if (attach.isRead) {
        attach.buffer.flip();
        Charset cs = Charset.forName("UTF-8");
        int limits = attach.buffer.limit();
        byte bytes[] = new byte[limits];
        attach.buffer.get(bytes, 0, limits);
        String msg = new String(bytes, cs);
        if (msg.charAt(1) == 'I')
        {
          System.out.println(msg);
          attach.id = Integer.parseInt(msg.replaceAll("[\\D]", ""));
        }
        else{
        System.out.format("Server Responded: "+ msg);
        }
        try {
          msg = PromptUser.mainPrompt(attach.id);
        
        attach.buffer.clear();
        byte[] data = msg.getBytes(cs);
        attach.buffer.put(data);
        attach.buffer.flip();
        attach.isRead = false; // It is a write
        attach.channel.write(attach.buffer, attach, this);
      } catch (Exception e) {
        System.out.println("Router hung up");
      }
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
  }