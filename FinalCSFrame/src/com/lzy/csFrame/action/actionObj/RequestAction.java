package com.lzy.csFrame.action.actionObj;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.lzy.csFrame.action.model.ActionDefinition;
import com.lzy.csFrame.action.util.PackageScanner;
import com.lzy.csFrame.action.util.ParamaterParser;
import com.lzy.csFrame.action.util.ParameterImmit;

import net.sf.json.JSONObject;

public class RequestAction implements IActionInterface{

	@Override
	public String exacute(String actionName,String para) {
		ActionDefinition actionDefinition= PackageScanner.get(actionName);
		Method method  = actionDefinition.getMethod();
		Object obj = actionDefinition.getObject();
		ParameterImmit immit = new ParameterImmit();
		JSONObject jsonParameter = JSONObject.fromObject(para);
		Object [] parameter= immit.methodInvoke(method, jsonParameter);
		Object result = null;
		try {
			result = method.invoke(obj, parameter);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		System.out.println(result);
		ParamaterParser paramaterParser = new ParamaterParser();
		return result == null ? null:paramaterParser.toJsonString("result", result).toString();
	}

}
