package com.chgreen.app;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Broker 
{
    
    public static void main( String[] args ) throws Exception
    {
        Scanner scanner = new Scanner(System.in);
        Socket sockRouter = new Socket("localhost", 5000);
        PrintStream PSRouter = new PrintStream(sockRouter.getOutputStream());
        String myMessage;
        while (true){
        myMessage =  scanner.next();
        PSRouter.println(myMessage);

        InputStreamReader IRRouter = new InputStreamReader(sockRouter.getInputStream());
        BufferedReader BRRouter = new BufferedReader(IRRouter);

        String message = BRRouter.readLine();
        System.out.println(message);
        }
    }
}
