package com.lzy.csFrame.common;
/**
 * 
 * @author 李芷源
 * communicationMessage 通信过程中communication不需要知道该消息内容是什么  
 * 只需知道  他从哪来，往那里去即可
 *
 */
public class CommunicationMessage {
     private String from;
     private String to;
     private String message;
     private ECommunicationCommand command;      //一共三种状态  接收成功 ，发送失败，接收失败
     
     public CommunicationMessage() {
	}

	public CommunicationMessage(String from, String to, String message) {
		this.from = from;
		this.to = to;
		this.message = message;
	}
	
	public CommunicationMessage(String to, String message) {
		this.to = to;
		this.message = message;
	}
	
	public CommunicationMessage(String message) {
		this.message = message;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public ECommunicationCommand getCommand() {
		return command;
	}

	public void setCommand(ECommunicationCommand command) {
		this.command = command;
	}

	@Override
	public String toString() {
		return from + ":" + to + "//" + message;
	}
}
