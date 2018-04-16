package com.lzy.csFrame.common;

public class ConversationMessage {
		private String action;
		private EConversationCommand command;
		private String message;
	
		public ConversationMessage() {
		}
	
		public ConversationMessage(String action, EConversationCommand command, String message) {
			//super();
			this.action = action;
			this.command = command;
			this.message = message;
		}
		
		public ConversationMessage(EConversationCommand command, String message) {
			this.command = command;
			this.message = message;
		}


		public String getAction() {
			return action;
		}
	
		public void setAction(String action) {
			this.action = action;
		}
	
		public EConversationCommand getCommand() {
			return command;
		}
	
		public void setCommand(EConversationCommand command) {
			this.command = command;
		}
	
		public String getMessage() {
			return message;
		}
	
		public void setMessage(String message) {
			this.message = message;
		}

		@Override
		public String toString() {
			return action + ":" + command + "&message=" + message;
		}
		
		

}
