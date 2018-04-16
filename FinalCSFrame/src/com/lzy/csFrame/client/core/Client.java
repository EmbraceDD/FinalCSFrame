package com.lzy.csFrame.client.core;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lzy.csFrame.action.util.ParamaterParser;
import com.lzy.csFrame.common.ConversationMessage;
import com.lzy.csFrame.common.EConversationCommand;
import com.lzy.csFrame.common.INetMessageListener;
import com.lzy.csFrame.util.CSFrameUtil;

import net.sf.json.JSONObject;

public abstract class Client implements INetMessageListener {
	   private static final Logger log = LogManager.getLogger(Client.class);
	   private static final String IP = "IP";
	   private static final String PORT="PORT";
	   
	   private static String  ip;
	   private static int port;
	   
	   private Socket socket;
	   private ConverClient converclient;
	   private boolean startUp;
	   private Thread thread;
	   
	   static{
		   ip = CSFrameUtil.getConfig(IP);
		   port = Integer.valueOf(CSFrameUtil.getConfig(PORT));
	   }
	   
	   public Client() {
		   this.startUp = false;
       } 
	   
       public Client(String ip, int port) {
		   Client.ip = ip;
		   Client.port = port;
		   this.startUp = false;
       } 
       
       public void startConnect(){
    	   try {
			socket = new Socket(ip, port);
			String cip = socket.getInetAddress().getHostAddress();
			System.out.println(cip);
			sendIP(cip);
			this.converclient = new ConverClient(socket);
			this.converclient.addListener(this);
			this.startUp = true;
			 keepAlive();
		} catch (IOException e) {
			e.printStackTrace();
		 }
       }
       /**
        * 
        * @param to        消息接收对象
        * @param command   消息类型
        * @param meessage  消息参数
        * 发送普通消息
        */
       public void sendMessage(String to,EConversationCommand command,String meessage){
    	   if(command == EConversationCommand.OFF_LINE){
    		   thread.interrupt();
    		   this.startUp = false;
    	   }
    	   
    	   ConversationMessage conversationMessage = new ConversationMessage(command, meessage);
    	   this.converclient.sendMessage(to ,conversationMessage);
       }
       
       public abstract void dealResponse(JSONObject json);
       public abstract void serverNetFailure();
       /**
        * 
        * @param to             消息接收方
        * @param action      	消息请求类型
        * @param command  	   	消息类型
        * @param messageObject  消息参数值
        */
       public void sendMessage(String to, String action, EConversationCommand command,
    		   Map<String, Object> messageObject){
    	   if(command == EConversationCommand.OFF_LINE){
    		   thread.interrupt();
    		   this.startUp = false;
    	   }
    	   ParamaterParser paramaterParser = new ParamaterParser();
    	   for(String parameterName: messageObject.keySet()){
    		  paramaterParser.toJsonString(parameterName, messageObject.get(parameterName));
    	   }
    	   ConversationMessage conversationMessage = new ConversationMessage(action, command, paramaterParser.toString());
    	   this.converclient.sendMessage(to ,conversationMessage);
       }
       
       public void stopConnec(){
    	   this.converclient.stopCommunication();
       }

	/**
	 * 发送自己本地ip
	 * @param ip
	 */
	private  void sendIP(String ip){
		DataOutputStream dos = null;
		try {
			dos = new DataOutputStream(this.socket.getOutputStream());
			dos.writeUTF(ip);
			log.info("发送自己的ip:"+ip);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 心跳包 ，可以维持广域网的长连接
	 */
	private void keepAlive() {
	
		Runnable runnable = new Thread(new Runnable() {
			@Override
			public void run() {
				while(startUp) {
					try {
					 Thread.sleep(60000);
					 ConversationMessage mess = new ConversationMessage(EConversationCommand.ALIVE, "alive");
					 converclient.sendMessage("", mess);
					} catch (InterruptedException e) {
						if(e.getMessage().equals( "sleep interrupted")){
							startUp = false;
						}
					}
				}
			}
		});
		thread = new Thread(runnable);
		thread.start();
	}

	@Override
	public void messageGained(String from, String message) {
		if(from.equals("response")){
			JSONObject json = JSONObject.fromObject(message);
			dealResponse(json);
			return;
		}else if(from.equals("offLine")){
			serverNetFailure();
		}else{
			System.out.println(from+":"+message);
		}
		
	}

}
