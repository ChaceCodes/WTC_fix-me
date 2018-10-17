package com.chgreen.app;

import java.io.*;
import java.net.*;

public class Router 
{
    public static void main( String[] args ) throws Exception
    {
        ServerSocket serverSocketBroker = new ServerSocket(5000);
        Socket socketBroker = serverSocketBroker.accept();
        InputStreamReader IRBroker = new InputStreamReader(socketBroker.getInputStream());
        BufferedReader BRBroker = new BufferedReader(IRBroker);
        System.out.println( "Hello World!" );

        String message = BRBroker.readLine();
        System.out.println(message);

        if (message != null)
        {
            PrintStream PSBroker = new PrintStream(socketBroker.getOutputStream());
            PSBroker.println("Message received!");
        }
    }
}
