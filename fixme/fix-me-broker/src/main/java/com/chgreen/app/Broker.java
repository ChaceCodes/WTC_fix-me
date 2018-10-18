package com.chgreen.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import com.chgreen.app.PromptUser;

public class Broker 
{
    
    public static void main( String[] args ) throws Exception
    {
        PromptUser userPrompt = new PromptUser();
        Scanner scanner = new Scanner(System.in);
        Socket sockRouter = new Socket("localhost", 5000);
        PrintStream PSRouter = new PrintStream(sockRouter.getOutputStream());
        String input = " ";
        
        
        while (true){
            userPrompt.mainPrompt();
            input =  scanner.nextLine();
            PSRouter.println(input);
            if (input.equalsIgnoreCase("exit")){
                sockRouter.close();
                scanner.close();
                return;
            }
   

            InputStreamReader IRRouter = new InputStreamReader(sockRouter.getInputStream());
            BufferedReader BRRouter = new BufferedReader(IRRouter);

            String message = BRRouter.readLine();
            System.out.println(message);
        }
    }
}
