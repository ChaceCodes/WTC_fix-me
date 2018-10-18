package com.chgreen.app;

import java.io.*;
import java.net.*;

public class Router 
{
    public static void main( String[] args ) throws Exception
    {
        int totalBrokers = 0;
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
        }
    }

    private int validateCheckSum(){

        return(0);
    }

    private int generateID(){

        return(0);
    } 
}
