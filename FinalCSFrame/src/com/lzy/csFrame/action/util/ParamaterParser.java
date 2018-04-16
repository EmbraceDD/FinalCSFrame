package com.lzy.csFrame.action.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.lzy.csFrame.request.model.StudentModel;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
/**
 * 
 * @author 李芷源
 {"map":{"student2":{"id":"haha","name":"hh","score":"99","sex":"1"},"student1":{"id":"haha","name":"hh","score":"99","sex":"1"}}}
{"list":[{"id":"haha","name":"hh","score":"99","sex":"1"},{"id":"haha","name":"hh","score":"99","sex":"1"}]}
{"id":3}
 */
public class ParamaterParser {
	  private  JSONObject json = new JSONObject();
	  public ParamaterParser() {
	 }
	  /**
	   * 
	   * @param name  actionName 
	   * @param obj   paramaterValue
	   * @return     josnString
	   */
      public  ParamaterParser toJsonString(String name,Object obj){
    	  if(isBaseClass(obj.getClass())){
    		  json.accumulate(name, obj);
    	  }else if(obj.getClass().isArray()||obj instanceof List){
    		  JSONArray array = JSONArray.fromObject(obj);
    		  json.accumulate(name, array);
    	  }else{
    		  JSONObject jsonObj = JSONObject.fromObject(obj);
    		  json.accumulate(name, jsonObj);
    	  }
    	  return this;
      }
      
      private boolean isBaseClass(Class<?> klass){
    	  return  klass.isPrimitive() 
    			  ||klass.equals(Byte.class)
    			  ||klass.equals(Character.class)
    			  ||klass.equals(Boolean.class)
    			  ||klass.equals(Short.class)
    			  ||klass.equals(Integer.class)
    			  ||klass.equals(Float.class)
    			  ||klass.equals(Long.class)
    			  ||klass.equals(Double.class)
    			  ||klass.equals(Object.class)
    			  ||klass.equals(String.class);
      }
      
      @Test
      public void test(){
    	  Map<String, StudentModel> st = new HashMap<>();
    	  st.put("student1", new StudentModel("haha", "hh", "1", "99"));
    	  st.put("student2", new StudentModel("haha", "hh", "1", "99"));
    	  List<StudentModel> list = new ArrayList<>();
    	  list.add( new StudentModel("haha", "hh", "1", "99"));
    	  list.add(new StudentModel("haha", "hh", "1", "99"));
    	  ParamaterParser paramaterParser = new ParamaterParser();
    	 String str =   paramaterParser.toJsonString("map",st).toString();
    	 String str1 = paramaterParser.toJsonString("list", list).toString();
    	 String str2 = paramaterParser.toJsonString("id", 3).toString();
    	 System.out.println(str);
    	 System.out.println(str1);
    	 System.out.println(str2);
    	 
      }
	@Override
	public String toString() {
		return  json.toString();
	}
      
}
