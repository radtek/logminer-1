package com.logminerplus.common;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class ObCommitQueue  
{
  static private LinkedBlockingDeque<ObDMLPacket> commitQueue_;
  public static boolean inited_ = false;
  public static void init()
  {
	commitQueue_ = new LinkedBlockingDeque<ObDMLPacket>(ObDefine.OB_MAX_COMMIT_TASK_NUM);
	inited_ = true;
	//commitQueue_.toArray()[0].getClass().

  }
  
  public static int pushCommitTask(ObDMLPacket packet) throws InterruptedException
  {
	int ret = ObDefine.OB_SUCCESS;
	if(ObDefine.OB_MAX_COMMIT_TASK_NUM == commitQueue_.size())
	{
	  ret = ObDefine.OB_OVER_FLOW;
	}
	else
	{
	  commitQueue_.put(packet);
	}
	return ret;
  }
  
  public static int[] getConflictThreadId(String tableName)
  {
	int ret[] = new int[2];
	int index = ObDefine.OB_INVALID_ID;
	ret[0] = ObDefine.OB_INVALID_ID;
	ret[1] = ObDefine.OB_INVALID_ID;
	Iterator<ObDMLPacket>itr =  commitQueue_.descendingIterator();
	while(itr.hasNext())
	{
      ObDMLPacket element = itr.next();
      index++;
      if(element.checkRowExist(tableName))
      {
    	ret[0] = index;
    	ret[1] = element.getThreadId();
    	//ObDefine.logger.info("test::whx =>" + tableName + "  "+ret[0] +"  "+ret[1]);
    	break;
      }
	}
	return ret;
  }
  
  public static ObDMLPacket getTopPacket()
  {
	return commitQueue_.peek();
	//commitQueue_.descendingIterator()
  }
  
  public static void pop() throws InterruptedException
  {
	commitQueue_.take();
	//commitQueue_.
  }
  
  public static void clean()
  {
	commitQueue_.clear();
  }
  
  public static int getSize()
  {
	return commitQueue_.size();
  }
}
