package com.logminerplus.gui.pane;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;

import com.logminerplus.bean.Fileset;
import com.logminerplus.bean.Log;
import com.logminerplus.common.ObProducer;
import com.logminerplus.core.Miner;
import com.logminerplus.gui.tablemodel.LogTableModel;
import com.logminerplus.jdbc.DataBase;
import com.logminerplus.utils.Context;
import com.logminerplus.utils.ContextConstants;
import com.logminerplus.utils.FileUtil;
import com.logminerplus.utils.PropertiesConstants;
import com.logminerplus.common.BloomFilter;
import com.logminerplus.common.ObCommitQueue;
import com.logminerplus.common.ObDefine;
import com.logminerplus.common.ObConsumer;
import com.logminerplus.common.ObDMLPacket;
import com.logminerplus.common.ObMonitor;
import com.logminerplus.common.ObNewProducer;
import com.logminerplus.common.ObPacketType;

public class ListenerManagePane {

	private static Logger logger = Logger.getLogger(ListenerManagePane.class
			.getName());

	private Fileset bean;
	private JScrollPane scrollPane;
	private JTable logTable;
	private LogTableModel dataModel;
	private List<String> columnNames;
	private List<Log> logInitList = new ArrayList<Log>();
	// Log List
	private Queue<Log> logLinkedList = new LinkedList<Log>();
	private WatchService watcher;
	private JLabel hintLable;
	private JButton minerBtn;
	private JButton stopBtn;
	public JTabbedPane init() throws Exception {
		JPanel mainPanel = new JPanel();

		initData();

		dataModel = new LogTableModel();
		dataModel.setColumnNames(columnNames);
		dataModel.setData(logInitList);
		logTable = new JTable();
		logTable.setRowHeight(Context.getInstance().getPropertyToInteger(
				PropertiesConstants.TABLE_ROWHEIGHT));
		logTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		logTable.setModel(dataModel);
		RowSorter<LogTableModel> sorter = new TableRowSorter<LogTableModel>(
				dataModel);
		logTable.setRowSorter(sorter);

		WatcherThread thread = new WatcherThread();
		new Thread(thread).start();

		scrollPane = new JScrollPane(logTable);

		JPanel southPane = new JPanel();
		southPane.setLayout(new FlowLayout(FlowLayout.RIGHT));

		hintLable = new JLabel();
		minerBtn = new JButton(Context.getInstance().getProperty(
				PropertiesConstants.BUTTON_RUN));
		minerBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					startMiner();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		stopBtn = new JButton(Context.getInstance().getProperty(
				PropertiesConstants.BUTTON_STOP));
		stopBtn.setEnabled(false);
		stopBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ContextConstants.isMineing = false;
			}
		});
		JButton refreshBtn = new JButton(Context.getInstance().getProperty(
				PropertiesConstants.BUTTON_REFRESH));
		refreshBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

			}
		});
		JButton updateDictBtn = new JButton("更新字典");
		updateDictBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Connection sourceConn = null;
				try {
					sourceConn = DataBase.getSourceDataBase();
					new Miner().createDictionary(sourceConn);
				} catch (Exception e2) {
					e2.printStackTrace();
					logger.error("Update Dict", e2);
					JOptionPane.showMessageDialog(Context.getInstance()
							.getMainFrame(), "更新字典：" + e2.getMessage(), "异常",
							JOptionPane.ERROR_MESSAGE);
				} finally {
					if (sourceConn != null) {
						try {
							sourceConn.close();
						} catch (Exception e3) {
							e3.printStackTrace();
							logger.error("Close Error", e3);
						}
					}
				}

			}
		});

		showHint();
		startMiner();

		southPane.add(hintLable);
		southPane.add(minerBtn);
		southPane.add(stopBtn);
		southPane.add(refreshBtn);
		// southPane.add(updateDictBtn);

		mainPanel.setLayout(new BorderLayout(0, 0));
		mainPanel.add(scrollPane, BorderLayout.CENTER);
		mainPanel.add(southPane, BorderLayout.SOUTH);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add(
				Context.getInstance().getProperty(
						PropertiesConstants.TABBEDPANE_LISTENER), mainPanel);
		return tabbedPane;
	}

	private void showHint() {
		Log log = logLinkedList.peek();
		if (log != null) {
			hintLable.setText("Total:" + logLinkedList.size() + ", Running:"
					+ log.getFilename());
		} else {
			hintLable.setText("Total:0");
		}
	}

	private void initData() throws Exception {
		bean = Context.getInstance().getFileSet();
		logger.info("Initialization Log Files");
		columnNames = new Vector<String>();
		columnNames.add(Context.getInstance().getProperty(
				PropertiesConstants.TABLE_LOG_UPDATETIME));
		columnNames.add(Context.getInstance().getProperty(
				PropertiesConstants.TABLE_LOG_FILENAME));
		columnNames.add(Context.getInstance().getProperty(
				PropertiesConstants.TABLE_LOG_FILEPATH));
		String path = Context.getInstance().getFileSet().getWatcherdir();
		File file = new File(path);
		File[] tempList = file.listFiles();
		if (tempList != null && tempList.length > 0) {
			for (int i = 0; i < tempList.length; i++) {
				if (tempList[i].isFile()) {
					File f = tempList[i];
					long modify = f.lastModified();
					Timestamp ts = new Timestamp(modify);
					Log log = new Log();
					log.setTs(ts);
					log.setFilename(f.getName());
					log.setAbsolutePath(bean.getLogdir() + "/" + f.getName());
					logInitList.add(log);
				}
			}
			Collections.sort(logInitList, new Comparator<Log>() {
				@Override
				public int compare(Log o1, Log o2) {
					return o1.getTs().compareTo(o2.getTs());
				}
			});
			// 添加到日志队列
			for (int i = 0; i < logInitList.size(); i++) {
				logLinkedList.offer(logInitList.get(i));
				logger.info("Add Log File == "
						+ logInitList.get(i).getAbsolutePath());
			}
			logger.info("Add Log File Count:" + logInitList.size());
		} else {
			logger.info("No log");
		}
		logger.info("End");
	}

	private void refresh() {
		try {
			if (logLinkedList != null) {
				dataModel.setData(new ArrayList<Log>(logLinkedList));
				logTable.setModel(dataModel);
				scrollPane.getVerticalScrollBar().setValue(
						scrollPane.getVerticalScrollBar().getMaximum());
				showHint();
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Refresh Error", e);
			JOptionPane.showMessageDialog(Context.getInstance().getMainFrame(),
					"Deleted Maybe!" + e.getMessage(), "异常",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void initWatcherService(Path path) throws IOException {
		watcher = FileSystems.getDefault().newWatchService();
		path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_DELETE,
				StandardWatchEventKinds.ENTRY_MODIFY);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void handleEvents() throws InterruptedException {
		while (true) {
			WatchKey key = watcher.take();
			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind kind = event.kind();
				if (kind == StandardWatchEventKinds.OVERFLOW) {
					continue;
				}
				WatchEvent<Path> e = (WatchEvent<Path>) event;
				Path fileName = e.context();
				logger.debug("Event " + kind
						+ " has happened,which fileName is " + fileName);
				if (fileName.toString().toLowerCase().endsWith(".dbf")) {
					File f = new File(bean.getWatcherdir() + "/" + fileName);
					if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
						// 文件创建时：添加一个元素
						if (f.isFile()) {
							long modify = f.lastModified();
							Timestamp ts = new Timestamp(modify);
							Log log = new Log();
							log.setTs(ts);
							log.setFilename(f.getName());
							log.setAbsolutePath(bean.getLogdir() + "/"
									+ f.getName());
							logLinkedList.offer(log);
							logger.debug("Watch File: " + f.getAbsolutePath());
							logger.debug("Length:" + f.length());
							refresh();
						}
					} else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
						// 文件修改时：开始分析
						if (f.isFile() && f.length() > 0) {
							startMiner();
						}
					}
				}
			}
			if (!key.reset()) {
				break;
			}
		}
	}

	private class WatcherThread implements Runnable {
		@Override
		public void run() {
			try {
				logger.info("Watcher Started:" + bean.getWatcherdir());
				Path path = Paths.get(bean.getWatcherdir());
				initWatcherService(path);
				handleEvents();
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Watcher Error", e);
			}
		}
	}

	private class MinerThread extends Thread {
		private ObNewProducer process = new ObNewProducer();
		private ObConsumer[] exec = new ObConsumer[ObDefine.OB_THREAD_NUM];
		private ObMonitor monitor = new ObMonitor();
		private int curExecId = 0;

		// spublic static BloomFilter<String> bloomFilter = null;

		public void init() {

			process.init();
			// curExecId = 0;
			for (int i = 0; i < ObDefine.OB_THREAD_NUM; i++) {
				exec[i] = new ObConsumer();
				exec[i].setIndex(i);
			}
			for (int i = 0; i < ObDefine.OB_THREAD_NUM; i++) {
				exec[i].start();
			}

			logger.info("init producer , consumer and monitorsuccess!");
		}

		private int runPratitionJob() throws Exception {
			int ret = ObDefine.OB_SUCCESS;
			int tid = ObDefine.OB_INVALID_ID;
			monitor.start();
			while (true) {
				ObDMLPacket task = new ObDMLPacket();
				process.getNextPacket(task);
				if (ObPacketType.LASTPACKET == task.getType()) {
					break;
				} else {
					while (ObDefine.OB_NEED_WAIT == (tid = getAvalExecId())) {
						ObDefine.OB_PRODUCE_LOCK.set(ObDefine.OB_INVALID_ID);
						while (ObDefine.OB_PRODUCE_LOCK.get() < 0) {
							Thread.sleep(10);
						}
						if (ObDefine.OB_PRODUCE_LOCK.get() >= 0) {
							if (ObDefine.OB_NEED_WAIT != (tid = getAvalExecId()))
								break;
						}
					}
					if (ObDefine.OB_INVALID_ID == ((int) task.getThreadId())) {
						task.setThreadID(tid);
						ObDefine.COMMIT_QUEUE.pushCommitTask(task);
						exec[tid].submitPacket(task);
					} else {
						tid = (int) task.getThreadId();
						while (!exec[tid].isTaskQueueFull()) {
							ObDefine.OB_PRODUCE_LOCK
									.set(ObDefine.OB_INVALID_ID);
							while (ObDefine.OB_PRODUCE_LOCK.get() < 0) {
								Thread.sleep(10);
							}
							if (ObDefine.OB_PRODUCE_LOCK.get() == tid) {
								break;
							}
						}
						task.setThreadID(tid);
						ObDefine.COMMIT_QUEUE.pushCommitTask(task);
						exec[tid].submitPacket(task);

					}
				}
			}

			return ret;
		}

		private int getAvalExecId() {
			int tid = ObDefine.OB_NEED_WAIT;
			int tidBak = curExecId;
			curExecId = (curExecId + 1) % ObDefine.OB_THREAD_NUM;

			if (exec[curExecId].isTaskQueueFull()) {
				tid = curExecId;
			} else {
				tid = ObDefine.OB_NEED_WAIT;
				curExecId = tidBak;
			}
			return tid;
		}

		@Override
		public void run() {
			if (!ContextConstants.isMineing) {
				// 正在挖掘/分析
				swicthMineingStatus(true);
				while (true) {
					try {
						if (!ContextConstants.isMineing) {
							// 手动中止
							swicthMineingStatus(false);
							break;
						}
						// 返回队列头部的元素
						Log log = logLinkedList.peek();
						if (log == null) {
							// 自动中止
							swicthMineingStatus(false);
							break;
						}
						// 只分析一个
						List<Log> nenLogList = new ArrayList<Log>();
						nenLogList.add(log);
						process.startLogMnr(nenLogList);
						logger.info("start log mnr");
						ObDefine.OB_PRODUCE_LOCK.set(1);
						if( ObDefine.OB_SUCCESS != runPratitionJob()) {
							throw new Exception();							
						}
						// 分析完备份
						else if(ObDefine.OB_SUCCESS != backupLogFile(log)) {
							throw new Exception();
						}
						// 从队列中移除
						logLinkedList.poll();
						refresh();
					} catch (Exception e) {
						logger.error(e);
						// 异常终止
						swicthMineingStatus(false);
						break;
					}
				}
			}
		}

		private int backupLogFile(Log log) {
			int ret = ObDefine.OB_SUCCESS;
			String oldPath = bean.getWatcherdir() + "/" + log.getFilename();
			String newPath = bean.getBakdir() + "/" + log.getFilename();
			try {
				FileUtil.moveFile(oldPath, newPath);
			} catch (Exception e) {
				ret = ObDefine.OB_ERROR;
				e.printStackTrace();
			}
			return ret;
		}
	}

	private void swicthMineingStatus(boolean bool) {
		ContextConstants.isMineing = bool;
		minerBtn.setEnabled(!bool);
		stopBtn.setEnabled(bool);
		if (bool) {
			minerBtn.setText(Context.getInstance().getProperty(
					PropertiesConstants.BUTTON_RUNNING));
		} else {
			minerBtn.setText(Context.getInstance().getProperty(
					PropertiesConstants.BUTTON_RUN));
		}
	}

	synchronized private void startMiner() throws InterruptedException {
		if (logLinkedList != null && !logLinkedList.isEmpty()) {
			MinerThread thread = new MinerThread();
			thread.init();
			thread.start();
			// new Thread(thread).start();
		} else {
			logger.info("Log List is null!");
		}
	}

}
