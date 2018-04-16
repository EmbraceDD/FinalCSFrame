package com.lzy.csFrame.action.model;

import java.lang.reflect.Method;

public class ActionDefinition {
	private Object object;
	private Method method;

	public ActionDefinition() {
	}

	public ActionDefinition(Object object, Method method) {
		// super();
		this.object = object;
		this.method = method;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	@Override
	public String toString() {
		return "object=" + object + ", method=" + method ;
	}
	
	

}
