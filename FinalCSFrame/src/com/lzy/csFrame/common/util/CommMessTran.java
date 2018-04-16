package com.lzy.csFrame.common.util;


import com.lzy.csFrame.common.CommunicationMessage;

public class CommMessTran {
       public static CommunicationMessage messToModel(String message) {
    	 CommunicationMessage mess = new CommunicationMessage();
    	 String arr[] = message.split("//");
    	 String ft = arr[0];
    	 int index = ft.indexOf(":");
    	 mess.setFrom(ft.substring(0, index));
    	 mess.setTo(ft.substring(index+1));
    	 mess.setMessage(arr[1]);
    	 return  mess;
       }
}
