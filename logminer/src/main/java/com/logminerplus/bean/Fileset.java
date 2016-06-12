package com.logminerplus.bean;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.log4j.Logger;

public class Fileset {
    private static Logger logger = Logger.getLogger(Fileset.class.getName());
	
	private String dictdir;
	
	private String logdir;
	
	private String watcherdir;
	
	private String bakdir;

    private List<Log> logList = new ArrayList<Log>();
    // Log List
    private Queue<Log> logQueue = new LinkedList<Log>();
 
	public String getWatcherdir() {
		return watcherdir;
	}

	public void setWatcherdir(String watcherdir) {
		this.watcherdir = watcherdir;
	}

	public String getDictdir() {
		return dictdir;
	}

	public void setDictdir(String dictdir) {
		this.dictdir = dictdir;
	}

	public String getLogdir() {
		return logdir;
	}

	public void setLogdir(String logdir) {
		this.logdir = logdir;
	}

	public String getBakdir() {
		return bakdir;
	}

	public void setBakdir(String bakdir) {
		this.bakdir = bakdir;
	}

	public List<Log> getLogList() {
		return logList;
	}

	public void setLogList(List<Log> logList) {
		this.logList = logList;
	}

	public Queue<Log> getLogQueue() {
		return logQueue;
	}

	public void setLogQueue(Queue<Log> logQueue) {
		this.logQueue = logQueue;
	}
	
    public void refreshLogListAndLogQueue()
    {
    	logger.debug("refresh loglist and logqueue.");
        String path = getWatcherdir();
        File file = new File(path);
        if(file.listFiles() != null)
        {
        	if (!getLogList().isEmpty())
        	{
        		getLogList().clear();
        	}
        	if (!getLogQueue().isEmpty())
        	{
        		getLogQueue().clear();
        	}
            for (File f : file.listFiles())
            {
                Log log = new Log(new Timestamp(f.lastModified()), f.getName(), getLogdir());
                getLogList().add(log);
            }
            Collections.sort(getLogList());
            // 添加到日志队列
            for (Log log : getLogList())
            {
                logger.info("Add Log File => " + log.getAbsolutePath());
                getLogQueue().offer(log);
            }
            logger.info("Add Log File Count => " + getLogList().size());
        }
        else
        {
        	logger.info("the watcher log dir is empty!");
        }
    }
}
