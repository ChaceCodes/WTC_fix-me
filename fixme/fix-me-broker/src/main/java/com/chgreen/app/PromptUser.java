package com.chgreen.app;

import java.util.Scanner;

public class PromptUser 
{
    private static Scanner scanner = new Scanner(System.in);
    public PromptUser(){
        return;
    }

    public static String mainPrompt(){
        String fixMsg = "";
        String tmp = "";
        int checksm = 0;
        System.out.println("Market ID:");
        while (!(isNumeric(tmp)))
        {
            tmp = scanner.nextLine();
        }
        fixMsg = tmp;

        tmp = "";
        System.out.println("1. Buy\n2. Sell");
        while (!(isNumeric(tmp)) || !(tmp.equals("1")) && !(tmp.equals("2")))
        {
        tmp = scanner.nextLine();
        }
        //validate in 
        if (tmp.equals("1")){
        fixMsg = fixMsg + "|buy";
        }
        else if (tmp.equals("2")){
            fixMsg = fixMsg + "|sell";
        }


        System.out.println("Instrument:");
        tmp = scanner.nextLine();
        fixMsg = fixMsg + "|" + tmp;

        tmp = "";
        System.out.println("Price:");
        while (!(isNumeric(tmp)))
        {
            tmp = scanner.nextLine();
        }
        fixMsg = fixMsg + "|" + tmp;

        tmp = "";
        System.out.println("Quantity:");
        while (!(isNumeric(tmp)))
        {
            tmp = scanner.nextLine();
        }
        //validate in 
        fixMsg = fixMsg + "|" + tmp;

        for (int i = 0; i < fixMsg.length(); i++){
            checksm += fixMsg.charAt(i);
        }
        fixMsg = fixMsg + "|" + checksm;
        return (fixMsg);
    }


private static boolean isNumeric(String str)  
{  
  try  
  {  
    double d = Double.parseDouble(str);  
  }  
  catch(NumberFormatException nfe)  
  {  
    return false;  
  }  
  return true;  
}

}