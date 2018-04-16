package com.lzy.csFrame.action.util;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.lzy.csFrame.action.actionObj.RequestAction;
import com.lzy.csFrame.action.annotation.ParameterAnnotation;
import com.lzy.csFrame.request.model.StudentModel;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ParameterImmit {
	public static final Logger logger = LogManager.getLogger(ParameterImmit.class);

	public ParameterImmit() {
	}

	public Object[] methodInvoke(Method method, JSONObject jsonParameter) {
		List<Object> list = new ArrayList<>();
		Parameter[] parameters = method.getParameters();
		for (Parameter parameter : parameters) {
			if (!parameter.isAnnotationPresent(ParameterAnnotation.class)) {
				logger.error(parameter.getName() + "方法参数无注解，无法执行");
				return null;
			}

			ParameterAnnotation annotation = parameter.getAnnotation(ParameterAnnotation.class);
			String name = annotation.name();
			Object object = jsonParameter.get(name);
			Class<?> klass = parameter.getType();
			Type type = parameter.getParameterizedType();

			list.add(dealParameter(object, klass, type));
		}

		return list.toArray();
	}
	/**
	@param <T>
	 * @param object
	 *            parameter的json对象
	 * @param klass
	 *            parameter类型
	 * @param type
	 *            parameter带泛型的类型
	 * @return 参数注入成功的parameter
	 *         ParameterizedType 参数类型为泛型
	 *         GenericArrayType，泛型数组，描述的是ParameterizedType类型以及TypeVariable类型数组
	 *         范型变量就是<E extends List>或者<E>
	 */
	private <T> Object dealParameter(Object object, Class<?> klass, Type type) {
		if (type instanceof ParameterizedType) {
			logger.info(type.getTypeName() +"此参数是泛型参数");
			if (klass == Map.class) {
				Map<T, T> map = dealMap(object, type);
				return map;
			} else if (klass == List.class) {
				List<T> list = dealList(object, type);
				return list;
			}
		}else if(type instanceof GenericArrayType){
			logger.info(type.getTypeName() +"此参数是类型变量的数组类型");
			return dealArray(object, klass, type);
		}else if(type instanceof TypeVariable<?>){
			logger.info(type.getTypeName() +"此参数是类型变量");
		}else if(type instanceof WildcardType){
			logger.info(type.getTypeName() +"此参数是通配符类型");
		}else if(klass instanceof Class){
			    if(isBaseClass(klass)){
					logger.info(type.getTypeName() +"此参数是普通类型类型");
					return object;
				}else if(klass.isArray()){
					logger.info(type.getTypeName() +"此参数是Array类型");
					return  dealArray(object, klass,type);
				}else{
					logger.info(type.getTypeName() +"此参数是用户自定义类型");
					return dealClass(klass,object);
				}
		}
		return object;
	}
 	@SuppressWarnings("unchecked")
	private <T> T dealClass(Class<?> klass, Object object) {
 		Object obj  = null;
		      try {
				obj = klass.newInstance();
				JSONObject json  =JSONObject.fromObject(object);
				Field [] fields = klass.getDeclaredFields();
				for(Field field : fields){
					if(!json.containsKey(field.getName())){
						continue;
					}
					Object parameter = json.get(field.getName());
					field.setAccessible(true);
					field.set(obj, parameter);
				}
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		      
		   return (T)obj; 
	}

	@SuppressWarnings("unchecked")
	private <T>  T []  dealArray(Object object, Class<?> klass,Type type) {
		Class<?> k =klass.getComponentType();;
		Type a = null ;
		List<T> jsonList= (List<T>) object;
		List<T> list = new ArrayList<>();
		
		if(type instanceof GenericArrayType){
			a = ((GenericArrayType)type).getGenericComponentType();
			for(int i =0; i< jsonList.size(); i++){
				list.add((T) dealParameter(jsonList.get(i), k, a));
			}
		}else{
			for(int i =0; i< jsonList.size(); i++){
				list.add((T) dealParameter( jsonList.get(i), k, (Type)k));
			}
		}
	    T [] lists = (T[]) JSONArray.toArray(JSONArray.fromObject(list), k);
	    
		return lists;
	}
	@SuppressWarnings("unchecked")
	private <T> List<T> dealList(Object object, Type type) {
		Type[] t = ((ParameterizedType) type).getActualTypeArguments();
		List<T> list= new ArrayList<>();
		List<T> jsonList= (List<T>) object;
		
	    for(int i =0; i< jsonList.size(); i++){
			Object value = dealParameter( jsonList.get(i), (Class<?>) t[0], t[0]);
			list.add((T)value);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	private <T> Map<T, T> dealMap(Object object, Type type) {
//		if(type instanceof ParameterizedType){
		Type[] t = ((ParameterizedType) type).getActualTypeArguments();
		
		Map<T, T> map = new HashMap<>();
		JSONObject jsonObj = JSONObject.fromObject(object);
		Iterator<String> keys = jsonObj.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			Object k = dealParameter(key, (Class<?>) t[0], t[0]);
			Object value = dealParameter(jsonObj.get(key), (Class<?>) t[1], t[1]);
			map.put((T) k, (T) value);
		}
		return (Map<T, T>) map;
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
	public <T> void test() {
		PackageScanner.scan("com.lzy.csFrame.request");
		String actionName = "setArray";
		String para = "{\"studentArray\":[{\"id\":\"haha\",\"name\":\"hh\",\"score\":\"99\",\"sex\":\"1\"},{\"id\":\"haha\",\"name\":\"hh\",\"score\":\"99\",\"sex\":\"1\"}]}";
		//para ="{\"studentArrayMap\":[{\"1\":{\"id\":\"haha\",\"name\":\"hh\",\"score\":\"99\",\"sex\":\"1\"}},{\"2\":{\"id\":\"haha\",\"name\":\"hh\",\"score\":\"99\",\"sex\":\"1\"}}]}";
		RequestAction action = new RequestAction();
		action.exacute(actionName, para);
		//para= "{\"studentMap\":{\"1\":{\"id\":\"haha\",\"name\":\"hh\",\"score\":\"99\",\"sex\":\"1\"},\"2\":{\"id\":\"haha\",\"name\":\"hh\",\"score\":\"99\",\"sex\":\"1\"}}}";
		
//		
//		Map<String, StudentModel> [] studentArr= new Map[3];
//		Map<String, StudentModel> one = new HashMap<>();
//		one.put("1", new StudentModel("1", "李芷源 ", "nv", "nice"));
//		one.put("2", new StudentModel("1", "李芷源 ", "nv", "nice"));
//		one.put("3", new StudentModel("1", "李芷源 ", "nv", "nice"));
//		studentArr[0] = one;
//		Map<String, StudentModel> two = new HashMap<>();
//		one.put("1", new StudentModel("1", "李芷源 ", "nv", "nice"));
//		one.put("2", new StudentModel("1", "李芷源 ", "nv", "nice"));
//		one.put("3", new StudentModel("1", "李芷源 ", "nv", "nice"));
//		studentArr[1] = two;
//		Map<String, StudentModel> three = new HashMap<>();
//		one.put("1", new StudentModel("1", "李芷源 ", "nv", "nice"));
//		one.put("2", new StudentModel("1", "李芷源 ", "nv", "nice"));
//		one.put("3", new StudentModel("1", "李芷源 ", "nv", "nice"));
//		studentArr[2] = three;
//		StudentDao dao = new StudentDao();
//		dao.setArrayMap(studentArr);
	}
}
