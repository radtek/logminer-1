package com.logminerplus.common;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import com.logminerplus.core.Miner;
import com.logminerplus.utils.Context;

public class ObDefine 
{
	 //=======================================for system env========================================================//
     public final static int OB_MAX_PACKET_NUM = Context.getInstance().getEnvToInteger("maxpacketnum");  
     
     public final static int OB_THREAD_NUM = Context.getInstance().getEnvToInteger("threadnum");
     
     public final static int OB_MAX_DML_NUM = Context.getInstance().getEnvToInteger("maxdmlnum");
     
     public final static int OB_SAFE_DML_NUM = OB_MAX_DML_NUM - 4;
     
     public final static int OB_MAX_COMMIT_TASK_NUM = 65535;
     
     public final static double OB_BLOOM_FILTER_POSITIVE = 0.1;
     
     public final static int OB_EXPECT_BM_SIZE = Context.getInstance().getEnvToInteger("expectbmsize");
     
     public final static int OB_SAFE_BATCH_UPDATE_NUM = Context.getInstance().getEnvToInteger("safebatchupdate");
     
     public final static int OB_HANDLE_UPDATE = Context.getInstance().getEnvToInteger("handleupdate");
     
     public static AtomicInteger OB_PRODUCE_LOCK = new AtomicInteger (OB_THREAD_NUM);
     
     public static ObCommitQueue COMMIT_QUEUE = new ObCommitQueue();
     
     public static Logger logger = Logger.getLogger(Miner.class.getName());
     
 	 public static org.apache.commons.logging.Log sqlsLog = LogFactory.getLog("sqls");
     
     public static int[] seq = new int[OB_THREAD_NUM];
     
     public static int debugId = 0;
     
     public static HashMap<String, Integer> hash = new HashMap<String, Integer>();
     
   //=======================================for code's logical control========================================================//
     
     public final static int OB_SUCCESS = 0;
     
     public final static int OB_ERROR = -1;
     
     public final static int OB_OVER_FLOW = -2;
     
     public final static int OB_ITER_END = -3;
     
     public final static int OB_INVALID_ID = -1;
     
     public final static int OB_BATCH_UPDATE = -4;
     
     public final static int OB_NEED_WAIT = -5;
     
     public final static int OB_COUNT = 50;
     
     public final static int OB_NEED_AGAIN = -6;
     
     public final static int OB_NEED_STOP= -7;
     
     public final static int OB_NEED_AGAIN_FOR_CONCAT = -8;
     
     public final static int OB_NEED_AGAIN_FOR_BATCH = -9;
     
     public final static int OB_PACKET_INIT = -10;
     
     public final static int OB_PACKET_OPEN = -11;
     
     public final static int OB_PACKET_CLOSE = -12;
     
     public final static int OB_START = 6;
     
     public final static int OB_COMMIT = 7;
     
     public final static int OB_UPDATE = 3;
     
     public final static int OB_UNSUPPORTED = 255;
     
     public final static String OB_ANE_USER = "ANE";
     
     
}
