package com.logminerplus.gui;

import java.awt.EventQueue;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.logminerplus.utils.ContextConstants;
import com.logminerplus.utils.FileUtil;
import com.logminerplus.utils.PropertiesUtil;

public class Main {
	
	private static Logger logger = Logger.getLogger(Main.class.getName());
	/**
	 * 启动开始时间
	 */
	public static long startTime = System.currentTimeMillis();

	public static void main(String[] args) {
		
		try {
			//读取日志配置文件
			String configFile = ContextConstants.CONFIG_LOG4J;
			Properties props  = PropertiesUtil.loadProp(configFile);
			String prefix = FileUtil.getRootPath();
			Set<Object> set = props.keySet();
			for (Object obj : set) {
				String key = obj.toString();
				if(key.endsWith(".file")) {
					String logFile = prefix + props.getProperty(key);
					props.setProperty(key ,logFile);
					System.out.println("Logs:" + logFile);
				}
			}
			PropertyConfigurator.configure(props);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//事件队列
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					logger.info("Starting");
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
					//启动完成时间
					long successTime = System.currentTimeMillis();
					long millisecond = successTime-startTime;
					logger.info("Server startup in "+millisecond+" ms");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
}
