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
        System.out.println("\nMarket ID:");
        while (!(isNumeric(tmp)))
        {
            tmp = scanner.nextLine();
        }
        fixMsg = tmp;

        tmp = "";
        System.out.println("\n1. Buy\n2. Sell");
        while (!(isNumeric(tmp)) || !(tmp.equals("1")) && !(tmp.equals("2")))
        {
        tmp = scanner.nextLine();
        }

        if (tmp.equals("1")){
        fixMsg = fixMsg + "|buy";
        }
        else if (tmp.equals("2")){
            fixMsg = fixMsg + "|sell";
        }


        System.out.println("\nInstrument:");
        tmp = scanner.nextLine();
        fixMsg = fixMsg + "|" + tmp;

        tmp = "";
        System.out.println("\nPrice:");
        while (!(isNumeric(tmp)))
        {
            tmp = scanner.nextLine();
        }
        fixMsg = fixMsg + "|" + tmp;

        tmp = "";
        System.out.println("\nQuantity:");
        while (!(isNumeric(tmp)))
        {
            tmp = scanner.nextLine();
        }
        
        fixMsg = fixMsg + "|" + tmp;

        for (int i = 0; i < fixMsg.length(); i++){
            //System.out.println(checksm);
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