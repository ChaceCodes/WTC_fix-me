package com.chgreen.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.concurrent.Future;
import org.json.*;

public class Market 
{
    
    public static void main( String[] args ) throws Exception
    {
      try{
        AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
        SocketAddress serverAddr = new InetSocketAddress("localhost", 5001);
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

class fixData{
    int idFrom;
    int idTo;
    String buySell;
    String status;
    String instrumentSymbol;
    int price;
    int quantity;
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

        if (msg.length() > 0 ){
          if (msg.charAt(1) == 'I' )
          {
            System.out.println(msg);
            attach.id = Integer.parseInt(msg.replaceAll("[\\D]", ""));
            attach.isRead = true;
            attach.buffer.clear();
            attach.channel.read(attach.buffer, attach, this);
          }
          else{
            System.out.println("Server Responded: " + msg);
          
          try {
            fixData sData = new fixData();
            String[] splitArray = msg.split("\\|");
            sData.idFrom = Integer.parseInt(splitArray[0]);
            sData.idTo = Integer.parseInt(splitArray[1]);
            sData.buySell = splitArray[2];
            sData.status = splitArray[3];
            sData.instrumentSymbol = splitArray[4];
            sData.price = Integer.parseInt(splitArray[5]);
            sData.quantity = Integer.parseInt(splitArray[6]);

            String sInfo = getSymInfo(sData.instrumentSymbol);
            JSONObject jObj = new JSONObject(sInfo);
            
            String symbolPrice = jObj.getJSONObject("Global Quote").getString("05. price");
            String symbolVolume = jObj.getJSONObject("Global Quote").getString("06. volume");

            
            if (sData.buySell.equalsIgnoreCase("buy")){
              if ((double)sData.price >= Double.parseDouble(symbolPrice) && sData.quantity <= Integer.parseInt(symbolVolume)){
                sData.status = "accepted";
              }
            }
            else if (sData.buySell.equalsIgnoreCase("sell")){
              if ((double)sData.price <= Double.parseDouble(symbolPrice) ){
                sData.status = "accepted";
              }
            }



            int checksm = 0;
            String fxMsg = sData.idTo + "|" + sData.idFrom + "|" + sData.buySell + "|" + sData.status + "|" + sData.instrumentSymbol + "|" + sData.price + "|" + sData.quantity;
            for (int i = 0; i < fxMsg.length(); i++){
              //System.out.println(checksm);
              checksm += fxMsg.charAt(i);
            }

            msg = fxMsg + "|" + checksm;
          } catch (Exception e) {
            e.printStackTrace();
          }

          //msg = "1001|1000|buy|aapl|12|12|1955";
          attach.buffer.clear();
          byte[] data = msg.getBytes(cs);
          attach.buffer.put(data);
          attach.buffer.flip();
          attach.isRead = false; // It is a write
          attach.channel.write(attach.buffer, attach, this);
        }
        }
        else{
          System.exit(0);
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

    private static String getSymInfo(String sym) throws Exception{
      String apiKey = "ZAI0DNSMLMVUOB2N";

      URL url = new URL("https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + sym + "&apikey=" + apiKey);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestProperty("Content-Type", "application/json");
      con.setRequestMethod("GET");
      BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String input;
      StringBuffer cont = new StringBuffer();
      while((input = br.readLine()) != null){
        cont.append(input);
      }
      br.close();
      con.disconnect();
      return(cont.toString());
    }
  }