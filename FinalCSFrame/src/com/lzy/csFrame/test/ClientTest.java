package com.lzy.csFrame.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.lzy.csFrame.client.core.Client;
import com.lzy.csFrame.common.EConversationCommand;
import com.lzy.csFrame.request.model.StudentModel;

import net.sf.json.JSONObject;

public class ClientTest {
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		String command = "";
		Client client = new Client() {

			@Override
			public void dealResponse(JSONObject json) {
				System.out.println(json.toString());
			}

			@Override
			public void serverNetFailure() {
			}
			
		};

		while (!command.equals("exit")) {
			command = sc.nextLine();
			if (command.equalsIgnoreCase("st")) {
				client.startConnect();
			} else if (command.equalsIgnoreCase("exit")) {
				client.stopConnec();
			} else if (command.equalsIgnoreCase("byebye")) {
				client.sendMessage("", EConversationCommand.OFF_LINE, command);
			} else {
				Map<String, Object> map = new HashMap<>();
				//map.put("id", "03153028");
				Map<String, StudentModel> m = new HashMap<>();
				m.put("1", new StudentModel("1", "蛋蛋", "1", "nice"));
				map.put("studentMap", m);
				client.sendMessage("", "setMap", EConversationCommand.REQUEST, map);
			}
		}
		sc.close();
	}
}
