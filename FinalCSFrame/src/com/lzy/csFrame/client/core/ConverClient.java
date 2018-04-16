package com.lzy.csFrame.client.core;

import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lzy.csFrame.common.Communication;
import com.lzy.csFrame.common.CommunicationMessage;
import com.lzy.csFrame.common.ConversationMessage;
import com.lzy.csFrame.common.ECommunicationCommand;
import com.lzy.csFrame.common.EConversationCommand;
import com.lzy.csFrame.common.ICommunicationListener;
import com.lzy.csFrame.common.INetMessageListener;
import com.lzy.csFrame.common.INetMessagePublisher;
import com.lzy.csFrame.common.util.ConvMessTran;

public  class ConverClient implements ICommunicationListener, INetMessagePublisher {
	private static final Logger log = LogManager.getLogger(ConverClient.class);
	private Communication communication;
	private INetMessageListener listener;

	public ConverClient(Socket socket) {
		communication = new Communication(socket, this);
		communication.startCommunication();
	}

	public void sendMessage(String to, ConversationMessage message) {
		CommunicationMessage communicationMessage = new CommunicationMessage(to, message.toString());
		this.communication.sendMessage(communicationMessage);
	}

	public void stopCommunication() {
		this.communication.stopCommunication();
	}
	
	//public abstract void dealResponse(){};

	@Override
	public void CommunicationMessageGained(CommunicationMessage message) {
		ECommunicationCommand command = message.getCommand();
		String from = message.getFrom();
		String mess = message.getMessage();

		switch (command) {
		case RECEIVE_OK:
			dealReceiveMessage(from, mess);
			break;
		case RECEIVE_FAILURE:
			this.listener.messageGained("offLine", mess);
			System.out.println("服务器" + mess);
			break;
		case SEND_FAILURE:
			break;
		default:
			break;
		}
	}

	private void dealReceiveMessage(String from, String mess) {
		ConversationMessage message = ConvMessTran.messToModel(mess);
		EConversationCommand command = message.getCommand();
		String info = message.getMessage();

		switch (command) {
		case ID:
			this.communication.setFrom(info); // 设置id
			break;
		case OFF_LINE:
			this.communication.setReceive(false);
			// 通知上层 正常下线
			break;
		case REQUEST:
			System.out.println(from + ":" + info);
			break;
		case SEND_TO_ALL:
			this.listener.messageGained("来自[" + from + "]", "的广播消息:" + info);
			break;
		case SEND_TO_ONE:
			this.listener.messageGained("来自[" + from + "]", "的消息:" + info);
			break;
		case SEND_TO_OTHER:
			this.listener.messageGained("来自[" + from + "]", "的群发消息:" + info);
			break;
		case RESPONSE:
			this.listener.messageGained("response", info);
			break;
		default:
			break;
		}
	}

	@Override
	public void addListener(INetMessageListener listener) {
		if (listener == null || this.listener == listener) {
			return;
		}
		this.listener = listener;
	}

	@Override
	public void removeListener(INetMessageListener listener) {
		if (listener == null || this.listener == null) {
			return;
		}
		this.listener = null;
	}

}
