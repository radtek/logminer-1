package com.logminerplus.common;

import org.apache.log4j.Logger;

import com.logminerplus.gui.pane.ListenerManagePane;

public class ObMonitor extends Thread{
	//private  Logger logger = Logger.getLogger(ListenerManagePane.class.getName());
	
	@Override
	public void run()
	{
	  int sum = 0;
	  int tmp = 0;
	  int []pre = new int[ObDefine.OB_THREAD_NUM];
	  while(true)
	  {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sum = 0;
		for(int i = 0; i < ObDefine.OB_THREAD_NUM; i++)
		{
		  tmp = ObDefine.seq[i];
		  sum += (tmp - pre[i]);
          //if(ObDefine.seq[i] - pre[i] > 0)
        	 // ObDefine.logger.info("single ops = "+(ObDefine.seq[i] - pre[i])+"dml tid = "+i);
		  
		  pre[i] = tmp;
		  
		}
		ObDefine.logger.info(" ops =\t"+ sum +"\tdml exec per second  "+ ObDefine.COMMIT_QUEUE.getSize());
		
		
		
	  }
	}
}
