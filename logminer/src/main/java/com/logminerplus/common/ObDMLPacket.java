package com.logminerplus.common;

import java.util.ArrayList;
import java.util.Iterator;

import com.logminerplus.common.ObDefine;
import com.logminerplus.core.Miner;

import org.apache.log4j.Logger;

public class ObDMLPacket 
{
  private ArrayList<String> redoArray_;
  
  private ArrayList<String> tableNameArray_;
  
  //public String tableName_;
  
  private long commitScn_;
  
  private int threadID_;
  
  public int conflictPos_;
  
  private static Logger logger = Logger.getLogger(Miner.class.getName());
  
  private ObPacketType type_;
  
  private boolean isEmpty_;
  
  private int seq_;
  
  private int dmlCount_;
  
  private int status_;
  
  private BloomFilter<String> bloomFilter_ = new BloomFilter<String>(ObDefine.OB_BLOOM_FILTER_POSITIVE,ObDefine.OB_EXPECT_BM_SIZE);
  
  public int batchUpdateCount = 0;
  
  public ObDMLPacket()
  {
	redoArray_ = new ArrayList<String> ();
	tableNameArray_ = new ArrayList<String> ();
	commitScn_ = -1;
	threadID_ = -1;
	type_ = ObPacketType.ERROR;
	isEmpty_ = true;
	//tableName_ = null;
	conflictPos_ = ObDefine.OB_INVALID_ID;
	dmlCount_ = 0;
	status_ = ObDefine.OB_PACKET_INIT;
  }
  
  public void reset()
  {
	redoArray_.clear();
	tableNameArray_.clear();
	commitScn_ = -1;
	threadID_ = -1;
	type_ = ObPacketType.ERROR;
	isEmpty_ = true;
	//tableName_ = null;
	bloomFilter_.clear();
	conflictPos_ = ObDefine.OB_INVALID_ID;
	dmlCount_ = 0;
	status_ = ObDefine.OB_PACKET_INIT;
	batchUpdateCount = 0;
  }
  
  public boolean isEmpty()
  {
	return isEmpty_;
  }
  
  public int getPacketSize()
  {
	return dmlCount_;
  }
  
  public int getTableNameArraySize()
  {
	return tableNameArray_.size();
  }
  
  public int getArraySize()
  {
	return redoArray_.size();
  }
  
  public void addDML(String redo, String tableName, int count) throws Exception 
  {
	/*if(ObDefine.OB_MAX_DML_NUM <= redoArray_.size())
	{
	  logger.error("DML array overflow, size = " + redoArray_.size());
	  throw new ObException("DML array overflow!");
	}
	else*/
	{
	  redoArray_.add(redo);
	  //rowIdArray_.add(rowId);
	  //bloomFilter_.add(tableName);
	  isEmpty_ = false;
	  if(0 == count)
	  {
		dmlCount_ ++;
	  }
	  else if(0 < count)
	  {
		dmlCount_ += count;
	  }
	}
  }
  
  public void addRowId(String rowId) throws Exception
  {
	  bloomFilter_.add(rowId);
  }
  
  public void addTableName(String tableName)
  {
	  tableNameArray_.add(tableName);
	 // bloomFilter_.add(tableName);
  }
  
  public boolean checkRowExist(String rowId)
  {
	return bloomFilter_.contains(rowId);
  }
  
  public void setCommitScn(long scn)
  {
	commitScn_ = scn;
  }
  
  public void setThreadID(int id)
  {
	threadID_ = id;
  }
  
  public void setPacketType(ObPacketType type)
  {
	type_ = type;
	if(type_ == ObPacketType.TRANSACTION)
	{
	 // redoArray_.add("start transaction;");
	}
  }
  
  public void rollBack()
  {
	if(0 < redoArray_.size())
	{
	  redoArray_.remove(redoArray_.size()-1);
	}
  }
  
  public void finish()
  {
//    if(type_ == ObPacketType.TRANSACTION)
//    {
//      redoArray_.add("commit;");
//	}
  }
  
  public void setSeq(int seq)
  {
	seq_ = seq;
  }
  
  public Iterator<String> getIter()
  {
	return redoArray_.iterator();
  }
  
  public Iterator<String> gettableNameIter()
  {
	return tableNameArray_.iterator();
  }
  
  public ObPacketType getType()
  {
	return type_;
  }
  
  public int getThreadId()
  {
	return threadID_;
  }
  
  public long getScn()
  {
	return commitScn_;
  }
  
  public int getSeq()
  {
	return seq_;
  }
  
  public int getStatus()
  {
	return status_;
  }
  
  public int open()
  {
	int ret = ObDefine.OB_SUCCESS;
	if(ObDefine.OB_PACKET_INIT != status_)
	{
	  ret = ObDefine.OB_ERROR;
	}
	else
	{
	  status_ = ObDefine.OB_PACKET_OPEN;
	}
	return ret;
  }
  
  public int close()
  {
	int ret = ObDefine.OB_SUCCESS;
	if(ObDefine.OB_PACKET_OPEN != status_)
	{
	  ret = ObDefine.OB_ERROR;
	}
	else
	{
	  status_ = ObDefine.OB_PACKET_CLOSE;
	}
	return ret;
  }
  
  public int copyPacket(ObDMLPacket other)
  {
	int ret = ObDefine.OB_SUCCESS;
	Iterator<String> itr = other.getIter();
	String it = null;
	while(itr.hasNext())
	{
	  it = itr.next();
	  redoArray_.add(it);
	}
	itr = other.gettableNameIter();
	it= null;
	while(itr.hasNext())
	{
	  it = itr.next();
	  if(!bloomFilter_.contains(it))
	  {
		bloomFilter_.add(it); 
	  }
	}
	seq_ = other.getSeq();
	dmlCount_ += other.getPacketSize();
	this.type_ = ObPacketType.TRANSACTION;
	if(ObDefine.OB_INVALID_ID != other.conflictPos_)
	{
	  if(ObDefine.OB_INVALID_ID == this.conflictPos_ || (this.conflictPos_ > other.conflictPos_))
	  {
		this.conflictPos_ = other.conflictPos_;
		this.threadID_ = other.getThreadId();
	  }
	}
	return ret;
  }
  

}
