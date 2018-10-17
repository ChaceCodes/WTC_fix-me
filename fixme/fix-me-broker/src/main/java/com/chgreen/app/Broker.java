package com.chgreen.app;

import java.io.*;
import java.net.*;

public class Broker 
{
    public static void main( String[] args ) throws Exception
    {
        Socket sockRouter = new Socket("localhost", 5000);
        PrintStream PSRouter = new PrintStream(sockRouter.getOutputStream());
        PSRouter.println("Hello to SERVER from Client.");

        InputStreamReader IRRouter = new InputStreamReader(sockRouter.getInputStream());
        BufferedReader BRRouter = new BufferedReader(IRRouter);

        String message = BRRouter.readLine();
        System.out.println(message);
    }
}
