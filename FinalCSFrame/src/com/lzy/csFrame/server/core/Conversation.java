package com.lzy.csFrame.server.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lzy.csFrame.action.actionObj.IActionInterface;
import com.lzy.csFrame.common.Communication;
import com.lzy.csFrame.common.CommunicationMessage;
import com.lzy.csFrame.common.ConversationMessage;
import com.lzy.csFrame.common.ECommunicationCommand;
import com.lzy.csFrame.common.EConversationCommand;
import com.lzy.csFrame.common.ICommunicationListener;
import com.lzy.csFrame.common.INetMessageListener;
import com.lzy.csFrame.common.INetMessagePublisher;
import com.lzy.csFrame.common.util.ConvMessTran;

public class Conversation implements ICommunicationListener, INetMessagePublisher {
		private static final Logger log = LogManager.getLogger(Conversation.class);
		private Map<String, Communication> communicationMap;
		private INetMessageListener listener;
		
		private IActionInterface action;
	
		
		public Conversation() {
			communicationMap = new HashMap<>();
		}
		
		public void setAction(IActionInterface action) {
			this.action = action;
		}

		public void setCommunication(String id, Communication communication) {
			this.communicationMap.put(id, communication);
		}
	
		public Map<String, Communication> getCommunicationMap() {
			return communicationMap;
		}
	
		public int getCommunicationMapSize() {
			return communicationMap.size();
		}
	
		private void sendToAllorOther(String from, String info, boolean all) {
			for (String key : communicationMap.keySet()) {
				if (!all && key.equals(from)) {
					continue;
				}
				Communication communication = communicationMap.get(key);
				ConversationMessage conversationMessage = new ConversationMessage(EConversationCommand.SEND_TO_ALL, info);
				CommunicationMessage communicationMessage = new CommunicationMessage(conversationMessage.toString());
				communication.sendMessage(communicationMessage);
			}
		}
	
		private void sendToOne(String to, String from, String info) {
			Communication communication = communicationMap.get(to);
			ConversationMessage conversationMessage = new ConversationMessage(EConversationCommand.SEND_TO_ONE, info);
			CommunicationMessage communicationMessage = new CommunicationMessage(from, to, conversationMessage.toString());
			communication.sendMessage(communicationMessage);
		}
	
		private void dealReceiveMessage(String to, String from, String mess) {
			ConversationMessage message = ConvMessTran.messToModel(mess);
			EConversationCommand command = message.getCommand();
			String info = message.getMessage();
			Communication communication = communicationMap.get(from);
	
			switch (command) {
			case OFF_LINE:
				ConversationMessage conv = new ConversationMessage("", EConversationCommand.OFF_LINE, "允许下线");
				CommunicationMessage communicationMessage = new CommunicationMessage(from, conv.toString());
				communication.sendMessage(communicationMessage);
				communication.stopCommunication();
				communicationMap.remove(from);
				this.listener.messageGained(from, "客户端正常下线");
				log.info(from + ":" + "客户端正常下线");
				break;
			case REQUEST:
				String actionName = message.getAction();
				String result = null;
				log.info("来自" + from + "的请求类型:" + action);
				if(action == null){
					log.error("action没有赋值");
					return;
				}
				result = action.exacute(actionName, info);
				ConversationMessage conversationMessage = new ConversationMessage(EConversationCommand.RESPONSE, result);
				CommunicationMessage communicationMessage2 = new CommunicationMessage(from, conversationMessage.toString());
				communication.sendMessage(communicationMessage2);
				break;
			case SEND_TO_ALL:
				log.info(from + "发送广播消息");
				sendToAllorOther(from, info, true);
				break;
			case SEND_TO_ONE:
				log.info(from + "给" + to + "发送消息");
				sendToOne(to, from, info);
				break;
			case SEND_TO_OTHER:
				log.info(from + "发送群发消息");
				sendToAllorOther(from, info, false);
				break;
			case ALIVE:
				System.out.println(from + ":" + info);
				break;
			default:
				break;
			}
		}
	   /**
	    * 整个方法的执行  都在 run线程中，所以在执行到gained方法调用的时候，将communication关闭，
	    * 不再进行readUTF,所以不会出现错误；
	    */
		@Override
		public  void CommunicationMessageGained(CommunicationMessage message) {
			ECommunicationCommand command = message.getCommand();
			String from = message.getFrom();
			String mess = message.getMessage();
			String to = message.getTo();
	
			switch (command) {
			case RECEIVE_OK:
				dealReceiveMessage(to, from, mess);
				break;
			case RECEIVE_FAILURE:
				communicationMap.remove(from);
				this.listener.messageGained(from, mess);
				log.info(from + ":" + mess);
				break;
			case SEND_FAILURE:
				communicationMap.remove(from);
				this.listener.messageGained(from, mess);
				log.info(from + ":" + mess);
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
