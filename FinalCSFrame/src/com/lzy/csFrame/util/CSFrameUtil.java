package com.lzy.csFrame.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CSFrameUtil {
       private static final Map<String, String> configMap;
       
       static{
    	   configMap = new HashMap<>();
    	   InputStream is = CSFrameUtil.class.getResourceAsStream("/net_config.properties");
    	   Properties properties = new Properties();
    	   try {
			properties.load(is);
			@SuppressWarnings("unchecked")
			Enumeration<String> keys = (Enumeration<String>) properties.propertyNames();
			
			while(keys.hasMoreElements()){
				String key = keys.nextElement();
				String value = properties.getProperty(key);
				configMap.put(key, value);
			}
		} catch (IOException e) {
			e.printStackTrace();
		 }
       }
       
       public static String  getConfig(String key){
    	  return  configMap.get(key);
       }
}
