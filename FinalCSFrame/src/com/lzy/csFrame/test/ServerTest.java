package com.lzy.csFrame.test;

import java.util.Scanner;

import com.lzy.csFrame.server.core.Server;

public class ServerTest {
   public static void main(String[] args) {
	    Scanner sc = new Scanner(System.in);
	    Server server = new Server();
	    String command = "";
	    
	    while(!command.equalsIgnoreCase("exit")){
	    	command = sc.nextLine();
	    	if(command.equalsIgnoreCase("st")){
	    		server.startUp();
	    	}else if(command.equalsIgnoreCase("exit")){
	    		server.forceDown();
	    	}
	    }
	    sc.close();
   }
}
