package com.lzy.csFrame.server.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lzy.csFrame.action.actionObj.RequestAction;
import com.lzy.csFrame.action.util.PackageScanner;
import com.lzy.csFrame.common.Communication;
import com.lzy.csFrame.common.CommunicationMessage;
import com.lzy.csFrame.common.ConversationMessage;
import com.lzy.csFrame.common.EConversationCommand;
import com.lzy.csFrame.common.INetMessageListener;
import com.lzy.csFrame.util.CSFrameUtil;
import com.lzy.csFrame.util.Config;

/**
 * 
 * @author 李芷源 主服务器类 主要方法 服务器开启，关闭，异常处理 监听客户端的连接 接收底层消息
 */
public class Server implements Runnable,INetMessageListener{
	    private static final Logger log = LogManager.getLogger(Server.class);
	
		private ServerSocket serverSocket;
		private static int port;
		private static String debug;
		private boolean isStart;
		private boolean goon;
		private Thread thread;
		private static String packageURL;
		
		
		private Conversation conversation;
		static {
			debug = CSFrameUtil.getConfig(Config.DEBUG);
			port = Integer.valueOf(CSFrameUtil.getConfig(Config.PORT));
			packageURL = CSFrameUtil.getConfig(Config.SCAN);
		}
	
		public Server() {
			this.isStart = false;
			this.goon = false;
		}
	
		public Server(int port) {
			this.isStart = false;
			this.goon = false;
			Server.port = port;
		}
		
		public void startUp() {
			if(this.isStart == true){
				return;
			}
			
			try {
				serverSocket = new ServerSocket(port);
				this.conversation =new Conversation();
				this.conversation.addListener(this);
				this.conversation.setAction(new RequestAction());
				log.info("服务器开启！");
				isStart = true;
				goon = true;
				thread = new Thread(this);
				thread.start();
				log.info("开始监听客户端连接");
				PackageScanner.scan(packageURL);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		public boolean isStart() {
			return isStart;
		}
		
		public void shutDown(){
			this.goon = false;
		}
		
		public void forceDown(){
			this.goon = false;
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    /**
     * 每次监听到一个客户端的连接  则new 一个communication  并且开启通信
     */
		@Override
		public void run() {
			while(goon){
				Socket socket;
				try {
					socket = serverSocket.accept();
					String ip = getIp(socket);
					if(debug.equals("0")&& this.conversation.getCommunicationMapSize() != 0){
						Map<String, Communication> map = this.conversation.getCommunicationMap();
						for(String key :map.keySet()){
							int index = key.indexOf("@");
							String k = key.substring(0, index);
							if(ip .equals(k)){
								dealConflict(ip,socket);
								break;
							}
							log.info("客户端"+"["+ip+ "]上线！");
							Communication communication = new Communication(socket, conversation);
							communication.startCommunication();
							String id = ip + "@" + communication.hashCode();
							ConversationMessage conv  = new ConversationMessage("", EConversationCommand.ID, id);
							CommunicationMessage communicationMessage = new CommunicationMessage("", conv.toString());
							communication.sendMessage(communicationMessage);
							this.conversation.setCommunication(id, communication);
						}
						
					}else{
						log.info("客户端"+"["+ip+ "]上线！");
						Communication communication = new Communication(socket, conversation);
						communication.startCommunication();
						String id = ip + "@" + communication.hashCode();
						ConversationMessage conv  = new ConversationMessage("", EConversationCommand.ID, id);
						CommunicationMessage communicationMessage = new CommunicationMessage("", conv.toString());
						communication.sendMessage(communicationMessage);
						communication.setFrom(id);
						this.conversation.setCommunication(id, communication);
					}
				} catch (IOException e) {
					if(goon == false){
						log.error("服务器正常下线！");
					}else{
						log.error("网络故障,服务器掉线！");
						goon = false;
					}
				}
			}
		}
		
		private void dealConflict(String ip,Socket socket){
			try {
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
				dos.writeUTF("该ip["+ip+ "]已登录，不能重复登录！");
				dos.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private String getIp(Socket socket) {
			String ip = null;
			DataInputStream dis = null;
			try {
				dis = new DataInputStream(socket.getInputStream());
				ip = dis.readUTF();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return ip;
		}

		@Override
		public void messageGained(String from, String message) {
			System.out.println(from+":"+message);
		}

}
