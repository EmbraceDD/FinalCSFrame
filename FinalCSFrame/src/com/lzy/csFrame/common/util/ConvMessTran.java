package com.lzy.csFrame.common.util;


import com.lzy.csFrame.common.ConversationMessage;
import com.lzy.csFrame.common.EConversationCommand;

public class ConvMessTran {
    public static ConversationMessage messToModel(String message){
    	ConversationMessage mess = new ConversationMessage();
    	String [] str = message.split("&");
    	String  ac = str[0];
    	int index = ac.indexOf(":");
    	mess.setAction(ac.substring(0, index));
    	mess.setCommand(EConversationCommand.valueOf(ac.substring(index+1)));
    	index = str[1].indexOf("=");
    	mess.setMessage(str[1].substring(index+1));
    	return mess;
    }
    
  
}
