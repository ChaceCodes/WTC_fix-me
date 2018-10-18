package com.chgreen.app;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import com.chgreen.app.PromptUser;

public class Broker 
{
    
    public static void main( String[] args ) throws Exception
    {
        Scanner scanner = new Scanner(System.in);
        Socket sockRouter = new Socket("localhost", 5000);
        PrintStream PSRouter = new PrintStream(sockRouter.getOutputStream());
        String input = " ";
        
        
        while (true){
            input =  scanner.nextLine();
            if (input.equalsIgnoreCase("exit")){
                return;
            }
            PSRouter.println(input);

            InputStreamReader IRRouter = new InputStreamReader(sockRouter.getInputStream());
            BufferedReader BRRouter = new BufferedReader(IRRouter);

            String message = BRRouter.readLine();
            System.out.println(message);
        }
    }
}
