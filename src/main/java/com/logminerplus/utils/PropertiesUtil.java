package com.logminerplus.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PropertiesUtil {
	
	private static Logger logger = Logger.getLogger(PropertiesUtil.class.getName());
	
	/**
	 * 加载属性
	 * @return
	 */
	public static Properties loadProp(String filename) {
		//属性集合对象
		Properties prop = new Properties();
		//读取
		try {
			//属性文件输入流
			FileInputStream fis = new FileInputStream(new File(FileUtil.getURIPath(filename)));
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			//将属性文件流装载到Properties对象中
			prop.load(isr);
			//关闭流
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("加载参数异常", e);
		}
		return prop;
	}
	
	/**
	 * 保存参数
	 * @param prop
	 */
	public static void saveProp(Properties prop, String filename) throws Exception {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(FileUtil.getURIPath(filename));
			prop.store(fos, "Properties");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("保存属性异常", e);
			throw new Exception("保存属性异常", e);
		} finally {
			try {
				if(fos!=null) {
					fos.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	
	/**
	 * 类型转换成数字
	 * @param str
	 * @return
	 * @throws Exception
	 */
	public static int toInteger(String str) throws Exception {
		int res = 0;
		try {
			res = Integer.parseInt(str);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("类型转换异常", e);
		}
		return res;
	}
}
