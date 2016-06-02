package com.logminerplus.common;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;

public class ObCommitQueue  
{
  private LinkedBlockingDeque<ObDMLPacket> commitQueue_;
  private boolean inited_ = false;
  public void init()
  {
	commitQueue_ = new LinkedBlockingDeque<ObDMLPacket>(ObDefine.OB_MAX_COMMIT_TASK_NUM);
	inited_ = true;
	//commitQueue_.toArray()[0].getClass().

  }
  
  public int pushCommitTask(ObDMLPacket packet) throws InterruptedException
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
  
  public int[] getConflictThreadId(String tableName)
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
  
  public ObDMLPacket getTopPacket()
  {
	return commitQueue_.peek();
	//commitQueue_.descendingIterator()
  }
  
  public void pop() throws InterruptedException
  {
	commitQueue_.take();
	//commitQueue_.
  }
  
  public void clean()
  {
	commitQueue_.clear();
  }
  
  public int getSize()
  {
	return commitQueue_.size();
  }
}
