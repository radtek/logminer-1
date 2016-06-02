package com.logminerplus.common;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import com.logminerplus.bean.Mapper;
import com.logminerplus.common.ObDMLPacket;
import com.logminerplus.common.ObDefine;
import com.logminerplus.jdbc.DataBase;
import com.logminerplus.utils.Context;

import org.apache.commons.logging.LogFactory;

public class ObConsumer extends Thread {
	private ArrayBlockingQueue<ObDMLPacket> taskQueue_;

	private org.apache.commons.logging.Log sqlsLog_ = LogFactory.getLog("sqls");

	private Connection autoCommitConnection_ = null;
	private Connection commitConnection_ = null;
	private ResultSet resultSet_ = null;
	private int seq_ = 0;
	private int seqBak_ = 0;
	private int index_ = 0;

	private String dictionary_ = null;
	private List<Mapper> mapperList_ = null;
	private String sourceName_ = null;
	private String targetName_ = null;

	private Object lock_ = new Object();

	public ObConsumer() {
		try {
			init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void init() throws Exception {
		taskQueue_ = new ArrayBlockingQueue<ObDMLPacket>(
				ObDefine.OB_MAX_PACKET_NUM);
		dictionary_ = Context.getInstance().getFileSet().getDictdir();
		mapperList_ = Context.getInstance().getMapperList();
		sourceName_ = Context.getInstance().getDataSource("source").getName()
				.toUpperCase();
		targetName_ = Context.getInstance().getDataSource("target").getName()
				.toUpperCase();
	}

	public int getSeq() {
		return seq_;
	}

	public void startConnection() {
		ObDefine.logger.info("################Start################");
		ObDefine.logger.info("thread id = " + this.getId());
		autoCommitConnection_ = null;
		commitConnection_ = null;
		resultSet_ = null;
		try {
			autoCommitConnection_ = DataBase.getTargetDataBase();
			commitConnection_ = DataBase.getTargetDataBase();
		} catch (NullPointerException e) {
			ObDefine.logger.error("null pointer!");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int submitPacket(ObDMLPacket packet) throws InterruptedException {
		int ret = ObDefine.OB_SUCCESS;
		if (ObDefine.OB_MAX_PACKET_NUM == taskQueue_.size()) {
			ret = ObDefine.OB_OVER_FLOW;
		}
		if (ObDefine.OB_SUCCESS == ret) {
			taskQueue_.put(packet);
		}
		return ret;
	}

	public void setIndex(int idx) {
		index_ = idx;
	}

	public void finishCommit() {
		if (ObDefine.OB_PRODUCE_LOCK.compareAndSet(ObDefine.OB_INVALID_ID, index_)) {
			ObDefine.logger.info("finishcommit index[" + index_ + "] success!");
		}
		else {
			ObDefine.logger.info("finishcommit index[" + index_ + "] failed!");			
		}
	}

	public boolean isTaskQueueFull() {
		return ObDefine.OB_MAX_PACKET_NUM > taskQueue_.size();
	}
	@Override
	public void run() {
		ObDefine.logger.info("consumer!");
		startConnection();
		ObDMLPacket task = null;
		ObDMLPacket cTask = null;
		Iterator<String> itr = null;
		String dmlSQL = null;
		Statement aotuConnStatement = null;
		Statement connStatement = null;
		String convert = null;
		try {
			commitConnection_.setAutoCommit(false);
			autoCommitConnection_.setAutoCommit(true);
			aotuConnStatement = autoCommitConnection_.createStatement();
			connStatement = commitConnection_.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long longtime = System.currentTimeMillis();
		long longtime2 = 0;
		boolean stop = false;
		while (!stop) {
//			ObDefine.logger.info("!taskQueue_.isEmpty() && 0 < ObCommitQueue.getSize() => " + (!taskQueue_.isEmpty() && 0 < ObCommitQueue.getSize()));
			while (!taskQueue_.isEmpty() && 0 < ObDefine.COMMIT_QUEUE.getSize()) {
				try {
					task = taskQueue_.take();
					itr = null;
				} catch (InterruptedException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				if (ObPacketType.SYSPACKET != task.getType()) {
					itr = task.getIter();
					while (itr.hasNext()) {
						convert = itr.next();
						try {
							connStatement.executeUpdate(convert);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							stop = true;
							ObDefine.sqlsLog.info("SQL exec error  "
									+ convert + "ThreadId " + this.getId()
									+ "seq " + task.getSeq());
							e.printStackTrace();
						}
					}
					while (true) {
						cTask = ObDefine.COMMIT_QUEUE.getTopPacket();
						if (cTask.getSeq() == task.getSeq()) {
							// TODO if equal, handle task and notify all
							try {
								commitConnection_.commit();
								commitConnection_.setAutoCommit(false);
								seq_ = task.getSeq();
								ObDefine.seq[index_] += task.getPacketSize();
								stop = true;
							} catch (SQLException e) {
								e.printStackTrace();
								break;

							}
							break;
						} else {
						}
					}
				}
				try {
					ObDefine.COMMIT_QUEUE.pop();
					finishCommit();
					cTask = null;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					stop = true;
					e.printStackTrace();
				}
			}
		}
	}
}
