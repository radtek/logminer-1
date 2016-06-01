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
	ArrayBlockingQueue<ObDMLPacket> taskQueue_;

	public org.apache.commons.logging.Log sqlsLog = LogFactory.getLog("sqls");

	private Connection autoCommitConnection = null;
	private Connection commitConnection = null;
	private ResultSet resultSet = null;
	private int seq = 0;
	private int seqBak = 0;
	private int index = 0;

	private static String dictionary = null;
	private static List<Mapper> mapperList = null;
	private static String sourceName = null;
	private static String targetName = null;

	public static Object lock = new Object();

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
		dictionary = Context.getInstance().getFileSet().getDictdir();
		mapperList = Context.getInstance().getMapperList();
		sourceName = Context.getInstance().getDataSource("source").getName()
				.toUpperCase();
		targetName = Context.getInstance().getDataSource("target").getName()
				.toUpperCase();
	}

	public int getSeq() {
		return seq;
	}

	public void startConnection() {
		ObDefine.logger.info("################Start################");
		ObDefine.logger.info("thread id = " + this.getId());
		autoCommitConnection = null;
		commitConnection = null;
		resultSet = null;
		try {
			autoCommitConnection = DataBase.getTargetDataBase();
			commitConnection = DataBase.getTargetDataBase();
		} catch (NullPointerException e) {
			ObDefine.logger.error("null pointer!");
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
		index = idx;
	}

	public void finishCommit() {
		if (ObDefine.OB_PRODUCE_LOCK.compareAndSet(ObDefine.OB_INVALID_ID, index)) {
			ObDefine.logger.info("finishcommit index[" + index + "] success!");
		}
		else {
			ObDefine.logger.info("finishcommit index[" + index + "] failed!");			
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
			commitConnection.setAutoCommit(false);
			autoCommitConnection.setAutoCommit(true);
			aotuConnStatement = autoCommitConnection.createStatement();
			connStatement = commitConnection.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long longtime = System.currentTimeMillis();
		long longtime2 = 0;
		boolean stop = false;
		while (!stop) {
//			ObDefine.logger.info("!taskQueue_.isEmpty() && 0 < ObCommitQueue.getSize() => " + (!taskQueue_.isEmpty() && 0 < ObCommitQueue.getSize()));
			while (!taskQueue_.isEmpty() && 0 < ObCommitQueue.getSize()) {
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
						cTask = ObCommitQueue.getTopPacket();
						if (cTask.getSeq() == task.getSeq()) {
							// TODO if equal, handle task and notify all
							try {
								commitConnection.commit();
								commitConnection.setAutoCommit(false);
								seq = task.getSeq();
								ObDefine.seq[index] += task.getPacketSize();
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
					ObCommitQueue.pop();
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
