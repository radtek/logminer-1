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
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;

import com.logminerplus.bean.Fileset;
import com.logminerplus.bean.Log;
import com.logminerplus.core.Miner;
import com.logminerplus.gui.tablemodel.LogTableModel;
import com.logminerplus.jdbc.DataBase;
import com.logminerplus.utils.Context;
import com.logminerplus.utils.ContextConstants;
import com.logminerplus.utils.FileUtil;
import com.logminerplus.utils.PropertiesConstants;
import com.logminerplus.common.ObDefine;
import com.logminerplus.common.ObConsumer;
import com.logminerplus.common.ObDMLPacket;
import com.logminerplus.common.ObMonitor;
import com.logminerplus.common.ObNewProducer;
import com.logminerplus.common.ObPacketType;

public class ListenerManagePane extends BasicPane {

    private static Logger logger = Logger.getLogger(ListenerManagePane.class.getName());

    private Fileset bean = null;
    private JPanel mainPanel = null;
    private JScrollPane scrollPane = null;
    private JPanel southPane = null;
    private JTable logTable = null;
    private JButton updateDictBtn = null;
    private LogTableModel dataModel = null;
    private List<String> columnNames = null;
    private WatchService watcher = null;
    private JLabel hintLable = null;
    private JButton minerBtn = null;
    private JButton stopBtn = null;
    private JButton refreshBtn = null;
    private JTabbedPane tabbedPane = null;

    private WatcherThread thread = null;
    
    public JTabbedPane init() throws Exception {
    	initData();

        dataModel = new LogTableModel();
        dataModel.setColumnNames(columnNames);
        dataModel.setData(bean.getLogList());

        logTable = new JTable();
        logTable.setRowHeight(Context.getInstance().getPropertyToInteger(PropertiesConstants.TABLE_ROWHEIGHT));
        logTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        logTable.setModel(dataModel);
        logTable.setRowSorter(new TableRowSorter<LogTableModel>(dataModel));

        scrollPane = new JScrollPane(logTable);

        hintLable = new JLabel();
        
        minerBtn = new JButton(Context.getInstance().getProperty(PropertiesConstants.BUTTON_RUN));
        minerBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startMiner();
                setEditStatus(false);
            }
        });
        
        stopBtn = new JButton(Context.getInstance().getProperty(PropertiesConstants.BUTTON_STOP));
        stopBtn.setEnabled(false);
        stopBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ContextConstants.isMineing = false;
                setEditStatus(true);
            }
        });

        refreshBtn = new JButton(Context.getInstance().getProperty(PropertiesConstants.BUTTON_REFRESH));
        refreshBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	bean.refreshLogListAndLogQueue();
            	dataModel.setData(bean.getLogList());
            }
        });
        
        updateDictBtn = new JButton("更新字典");
        updateDictBtn.addActionListener(new UpdateDictActionListener());

        thread = new WatcherThread();
        thread.start();

        showHint();
        startMiner();

        southPane = new JPanel();
        southPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
        southPane.add(hintLable);
        southPane.add(minerBtn);
        southPane.add(stopBtn);
        southPane.add(refreshBtn);
        southPane.add(updateDictBtn);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(0, 0));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(southPane, BorderLayout.SOUTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.add(Context.getInstance().getProperty(PropertiesConstants.TABBEDPANE_LISTENER), mainPanel);
        return tabbedPane;
    }

    private void showHint() {
        Log log = bean.getLogQueue().peek();
        if (log != null) {
            hintLable.setText("Total:" + bean.getLogQueue().size() + ", Running:" + log.getFilename());
        } else {
            hintLable.setText("Total:0");
        }
    }

    public void initData() throws Exception {
        bean = Context.getInstance().getFileSet();
        logger.info("Initialization Log Files");
        columnNames = new Vector<String>();
        columnNames.add(Context.getInstance().getProperty(PropertiesConstants.TABLE_LOG_UPDATETIME));
        columnNames.add(Context.getInstance().getProperty(PropertiesConstants.TABLE_LOG_FILENAME));
        columnNames.add(Context.getInstance().getProperty(PropertiesConstants.TABLE_LOG_FILEPATH));
        bean.refreshLogListAndLogQueue();
        logger.info("End");
    }
    

    private void refresh() {
        try {
            if (bean.getLogQueue() != null) {
                dataModel.setData(bean.getLogList());
                logTable.setModel(dataModel);
                scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
                showHint();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Refresh Error", e);
            JOptionPane.showMessageDialog(Context.getInstance().getMainFrame(), "Deleted Maybe!" + e.getMessage(), "异常", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initWatcherService(Path path) {
        try {
            watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            logger.error("new watchService failed!");
            e.printStackTrace();
        }
        try {
            path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            logger.error("register watcher failed!");
            e.printStackTrace();
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void handleEvents() {
        while (true) {
            WatchKey key = null;
            while (key == null) {
                try {
                    key = watcher.take();
                } catch (InterruptedException e1) {
                    logger.warn("watcher is interrupted! retry!");
                    e1.printStackTrace();
                }
            }
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }
                WatchEvent<Path> e = (WatchEvent<Path>) event;
                Path fileName = e.context();
                logger.debug("Event " + kind + " has happened,which fileName is " + fileName);
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
                            log.setAbsolutePath(bean.getLogdir() + "/" + f.getName());
                            bean.getLogQueue().offer(log);
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
    
    private class UpdateDictActionListener implements ActionListener
    {
		@Override
		public void actionPerformed(ActionEvent event) {
            Connection sourceConn = null;
            try {
                sourceConn = DataBase.getSourceDataBase();
                new Miner().createDictionary(sourceConn);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Update Dict", e);
                JOptionPane.showMessageDialog(Context.getInstance().getMainFrame(), "更新字典：" + e.getMessage(), "异常", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (sourceConn != null) {
                    try {
                        sourceConn.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.error("Close Error", e);
                    }
                }
            }
		}}

    private class WatcherThread extends Thread {
        @Override
        public void run() {
            logger.info("Watcher Started, watch dir => " + bean.getWatcherdir());
            Path path = Paths.get(bean.getWatcherdir());
            initWatcherService(path);
            handleEvents();
        }
    }

    private class MinerThread extends Thread {
        private ObNewProducer producer = new ObNewProducer();
        private ObConsumer[] consumers = new ObConsumer[ObDefine.OB_THREAD_NUM];
        private ObMonitor monitor = new ObMonitor();
        private int curExecId = 0;

        // spublic static BloomFilter<String> bloomFilter = null;

        public void init() {

            producer.init();
            // curExecId = 0;
            for (int i = 0; i < ObDefine.OB_THREAD_NUM; i++) {
                consumers[i] = new ObConsumer();
                consumers[i].setIndex(i);
            }
            for (int i = 0; i < ObDefine.OB_THREAD_NUM; i++) {
                consumers[i].start();
            }

            logger.info("init producer , consumer and monitorsuccess!");
        }

        private int runPratitionJob() throws Exception {
            int ret = ObDefine.OB_SUCCESS;
            int tid = ObDefine.OB_INVALID_ID;
            monitor.start();
            while (true) {
                ObDMLPacket task = new ObDMLPacket();
                producer.getNextPacket(task);
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
                        consumers[tid].submitPacket(task);
                    } else {
                        tid = (int) task.getThreadId();
                        while (!consumers[tid].isTaskQueueFull()) {
                            ObDefine.OB_PRODUCE_LOCK.set(ObDefine.OB_INVALID_ID);
                            while (ObDefine.OB_PRODUCE_LOCK.get() < 0) {
                                Thread.sleep(10);
                            }
                            if (ObDefine.OB_PRODUCE_LOCK.get() == tid) {
                                break;
                            }
                        }
                        task.setThreadID(tid);
                        ObDefine.COMMIT_QUEUE.pushCommitTask(task);
                        consumers[tid].submitPacket(task);

                    }
                }
            }

            return ret;
        }

        private int getAvalExecId() {
            int tid = ObDefine.OB_NEED_WAIT;
            int tidBak = curExecId;
            curExecId = (curExecId + 1) % ObDefine.OB_THREAD_NUM;

            if (consumers[curExecId].isTaskQueueFull()) {
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
                        Log log = bean.getLogQueue().peek();
                        if (log == null) {
                            // 自动中止
                            swicthMineingStatus(false);
                            break;
                        }
                        // 只分析一个
                        List<Log> nenLogList = new ArrayList<Log>();
                        nenLogList.add(log);
                        producer.startLogMnr(nenLogList);
                        logger.info("start log mnr");
                        ObDefine.OB_PRODUCE_LOCK.set(1);
                        if (ObDefine.OB_SUCCESS != runPratitionJob()) {
                            throw new Exception();
                        }
                        // 分析完备份
                        else if (ObDefine.OB_SUCCESS != backupLogFile(log)) {
                            throw new Exception();
                        }
                        // 从队列中移除
                        bean.getLogQueue().poll();
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
            minerBtn.setText(Context.getInstance().getProperty(PropertiesConstants.BUTTON_RUNNING));
        } else {
            minerBtn.setText(Context.getInstance().getProperty(PropertiesConstants.BUTTON_RUN));
        }
    }

    synchronized private void startMiner() {
        if (bean.getLogQueue() != null && !bean.getLogQueue().isEmpty()) {
            MinerThread thread = new MinerThread();
            thread.init();
            thread.start();
        } else {
            logger.info("Log List is null!");
        }
    }

	@Override
	public void setEditStatus(boolean isEnable) {
        minerBtn.setEnabled(isEnable);
        stopBtn.setEnabled(!isEnable);
	}

}
