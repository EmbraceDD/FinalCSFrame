package com.lzy.csFrame.action.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lzy.csFrame.action.annotation.ComponentClass;
import com.lzy.csFrame.action.annotation.MethodAction;
import com.lzy.csFrame.action.model.ActionDefinition;

public class PackageScanner {
	private static final Map<String, ActionDefinition> actionMap;
	public static final Logger logger = LogManager.getLogger(PackageScanner.class);

	static {
		actionMap = new HashMap<>();
	}

	public static void scan(String fileName) {
		String filePath = fileName.replace(".", "/");
		Enumeration<URL> urls;
		try {
			urls = PackageScanner.class.getClassLoader().getResources(filePath);

			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				String protocol = url.getProtocol();
				if (protocol.equals("file")) {
					logger.info("扫描的是文件");
					String path = URLDecoder.decode(url.getFile(), "UTF-8");
					dealFile(fileName, path);
				} else if (protocol.equals("jar")) {
					logger.info("扫描的是jar包");
					try {
						JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
						Enumeration<JarEntry> entries = jar.entries();
						while (entries.hasMoreElements()) {
							JarEntry entry = entries.nextElement();
							String name = entry.getName();
							// 所找的项目是以filepath开始的，主要排除掉META-INF/，等不相干选项
							if (name.startsWith(filePath)) {
								// 当前选项不是目录，并且以.class结尾。
								if (!entry.isDirectory() && name.endsWith(".class")) {
									name = name.replace("/", ".");
									String className = name.substring(0, name.indexOf(".class"));
									System.out.println(className);
									try {
										Class<?> klass = Class.forName(className);
										dealClass(klass);
									} catch (ClassNotFoundException e) {
										e.printStackTrace();
									}

								}
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param fileName
	 *            一种扫描方式，以Thread.currentThread().getContextClassLoader().
	 *            getResource(filePath);获得资源路径 并且判断资源在计算机中以什么协议存储的，并处理jar包中的文件
	 */
	public static void scan1(String fileName) {
		String filePath = fileName.replace(".", "/");

		URL url = Thread.currentThread().getContextClassLoader().getResource(filePath);
		String protocol = url.getProtocol();
		try {
			if (protocol.equals("file")) {
				logger.info("扫描的是文件");
				String path = URLDecoder.decode(url.getFile(), "UTF-8");
				dealFile(fileName, path);
			} else if (protocol.equals("jar")) {
				logger.info("扫描的是jar包");
				try {
					JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
					Enumeration<JarEntry> entries = jar.entries();
					/**
					 * 所找的项目是以filepath开始的，主要排除掉META-INF/，等不相干选项
					 * 这个方法加载的时候不会触发static 方法等 当前选项不是目录，并且以.class结尾。
					 */
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						String entryName = entry.getName();
						if (entryName.startsWith(filePath)) {
							if (!entry.isDirectory() && entryName.endsWith(".class")) {
								entryName = entryName.replace("/", ".");
								String className = entryName.substring(0, entryName.indexOf(".class"));
								try {
									Class<?> klass = Thread.currentThread().getContextClassLoader()
											.loadClass(className);
									dealClass(klass);
								} catch (ClassNotFoundException e) {
									e.printStackTrace();
								}
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param fileName
	 *            以.分隔的类路径
	 * @param filePath
	 *            以/分隔的类路径
	 */
	private static void dealFile(String fileName, String filePath) {
		File dir = new File(filePath);
		if (!dir.exists() || !dir.isDirectory()) {
			logger.warn("文件或者目录不存在");
			return;
		}
		/**
		 * 过滤文件，只保留保留目录，或者后缀名是.class结尾的文件(java文件)
		 */
		File[] files = dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return dir.isDirectory() || dir.getName().endsWith(".class");
			}
		});

		for (File file : files) {
			if (file.isDirectory()) {
				dealFile(fileName + "." + file.getName(), file.getAbsolutePath());
				// 递归查找
			} else {
				String name = file.getName();
				int index = name.indexOf(".class");
				String className = fileName + "." + name.substring(0, index);
				try {
					Class<?> klass = Class.forName(className);
					dealClass(klass);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 
	 * @param klass
	 *            传入路径下的java类 检测类和方法是否含有相应的注解。
	 */

	private static void dealClass(Class<?> klass) {
		Constructor<?> constructor = null;
		try {
			constructor = klass.getConstructor();
		} catch (NoSuchMethodException e1) {
			logger.error(klass.getName() + "没有无参构造方法，不能进行初始化");
			return;
		} catch (SecurityException e1) {
			e1.printStackTrace();
		}

		try {
			Object obj = constructor.newInstance();
			if (!klass.isAnnotationPresent(ComponentClass.class)) {
				return;
			}
			Method[] methods = klass.getDeclaredMethods();
			for (Method method : methods) {
				if (method.isAnnotationPresent(MethodAction.class)) {
					MethodAction action = method.getAnnotation(MethodAction.class);
					String name = action.name();
					ActionDefinition actionDefinition = new ActionDefinition(obj, method);
					actionMap.put(name, actionDefinition);
					logger.info(klass.getName() + "的" + method.getName() + "方法有对应的action:" + name);
				}
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

	}

	public static ActionDefinition get(String actionName) {
		return actionMap.get(actionName);
	}
}
