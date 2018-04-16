package com.lzy.csFrame.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.lzy.csFrame.common.util.CommMessTran;

/**
 * 
 * @author 李芷源
 *communication 支持整个框架的底层通信信道，接收发送消息，并且传递信息
 *自主下线，解决通信中断异常
 */
public class Communication implements Runnable  , ICommunicationPublisher{
	
	private Socket socket;
	private ICommunicationListener listener;
	private DataInputStream dis;
	private DataOutputStream dos;
	private Thread thread;
	
	private boolean receive;
	private String from;

	public Communication(Socket socket, ICommunicationListener listener) {
		this.socket = socket;
		this.listener = listener;
		this.receive  = false;
	}
	
	public void setFrom(String from) {
		this.from = from;
	}
	
	public void startCommunication(){
		try {
			this.dis = new DataInputStream(socket.getInputStream());
			this.dos = new DataOutputStream(socket.getOutputStream());
			this.receive = true;
			this.thread = new Thread(this);
			this.thread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void setReceive(boolean receive) {
		this.receive = receive;
	}

	/**
	 * 要发送给对端的消息
	 * @param message
	 */
	public void sendMessage(CommunicationMessage communicationMessage){
		communicationMessage.setFrom(from);
		try {
			this.dos.writeUTF(communicationMessage.toString());
		} catch (IOException e) {
//			CommunicationMessage mess = new CommunicationMessage(from, "", "异常下线");
//			mess.setCommand(ECommunicationCommand.SEND_FAILURE);
//			this.listener.CommunicationMessageGained(mess);
		}
	}
	/**
	 * @return   接收到对端发送的消息
	 * @throws IOException 通信中断异常
	 */
	public String receiveMessage() throws IOException{
		String messge = null;
		messge = this.dis.readUTF();
		return messge;
	}
	/**
	 * 关闭信道  先关闭输出，再关闭输入
	 */
	public void stopCommunication(){
		this.receive = false;
		try {
			if(this.dos  != null){
				this.dos.close();
			}
			
			if(this.dis  != null){
				this.dis.close();
			}
			
			if(this.socket  != null){
				this.socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			this.dos = null;
			this.dis = null;
			this.socket = null;
		}
	}

	@Override
	public void addCommunicationListenner(ICommunicationListener listener) {
		if(listener == null || this.listener == listener){
			return;
		}
		this.listener = listener;
	}

	@Override
	public void removeCommunicationListenner(ICommunicationListener listener) {
		if(listener == null|| this.listener == null){
			return;
		}
		this.listener = null;
	}
    /**
     * 接收消息是一个被动的动作，需要用线程实时监控
     * 将接收传递
     */
	@Override
	public void run() {
		String message = null;
		while(receive){
			try {
				message = receiveMessage();
				CommunicationMessage communicationMessage = CommMessTran.messToModel(message);
				communicationMessage.setCommand(ECommunicationCommand.RECEIVE_OK);
				this.listener.CommunicationMessageGained(communicationMessage);
			} catch (IOException e) {
				stopCommunication();
				CommunicationMessage mess = new CommunicationMessage(from, "", "异常下线");
				mess.setCommand(ECommunicationCommand.RECEIVE_FAILURE);
				this.listener.CommunicationMessageGained(mess);
			}
		}
		  stopCommunication();
	}

}
