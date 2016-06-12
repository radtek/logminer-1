package com.logminerplus.gui.pane;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;

import com.logminerplus.bean.Log;
import com.logminerplus.gui.tablemodel.LogTableModel;
import com.logminerplus.utils.Context;
import com.logminerplus.utils.FileUtil;
import com.logminerplus.utils.PropertiesConstants;
import com.logminerplus.utils.PropertiesUtil;

public class LogManagePane {

    private static Logger logger = Logger.getLogger(LogManagePane.class.getName());

    private JScrollPane scrollPane;
    private JTable logTable;
    private LogTableModel dataModel;
    private List<String> columnNames;
    private List<Log> logList = new ArrayList<Log>();

    public JTabbedPane init() throws Exception {
        JPanel mainPanel = new JPanel();

        initData();

        dataModel = new LogTableModel();
        dataModel.setColumnNames(columnNames);
        dataModel.setData(logList);
        logTable = new JTable();
        logTable.setRowHeight(Context.getInstance().getPropertyToInteger(PropertiesConstants.TABLE_ROWHEIGHT));
        logTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        logTable.setModel(dataModel);
        RowSorter<LogTableModel> sorter = new TableRowSorter<LogTableModel>(dataModel);
        logTable.setRowSorter(sorter);
        logTable.addMouseListener(new MouseListener() {
            public void mouseReleased(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            public void mousePressed(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            public void mouseExited(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            public void mouseEntered(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openLogDialog();
                }
            }
        });
        scrollPane = new JScrollPane(logTable);

        JPanel southPane = new JPanel();
        southPane.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton showBtn = new JButton(Context.getInstance().getProperty(PropertiesConstants.BUTTON_SHOW));
        showBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openLogDialog();
            }
        });

        JButton refreshBtn = new JButton(Context.getInstance().getProperty(PropertiesConstants.BUTTON_REFRESH));
        refreshBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });

        southPane.add(showBtn);
        southPane.add(refreshBtn);

        mainPanel.setLayout(new BorderLayout(0, 0));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(southPane, BorderLayout.SOUTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add(Context.getInstance().getProperty(PropertiesConstants.TABBEDPANE_LOGMANAGER), mainPanel);
        return tabbedPane;
    }

    private void initData() throws Exception {
        //
        columnNames = new Vector<String>();
        columnNames.add(Context.getInstance().getProperty(PropertiesConstants.TABLE_LOG_UPDATETIME));
        columnNames.add(Context.getInstance().getProperty(PropertiesConstants.TABLE_LOG_FILENAME));
        columnNames.add(Context.getInstance().getProperty(PropertiesConstants.TABLE_LOG_FILEPATH));
        //
        File file = new File(FileUtil.getURIPath("logs/"));
        File[] tempList = file.listFiles();
        if (tempList != null && tempList.length > 0) {
            for (int i = 0; i < tempList.length; i++) {
                if (tempList[i].isFile()) {
                    File f = tempList[i];
                    logger.info("initData() f.getPath() => " + f.getParent());
                    Log log = new Log(new Timestamp(f.lastModified()), f.getName(), f.getParent());
                    logList.add(log);
                }
            }
            Collections.sort(logList, new Comparator<Log>() {
                @Override
                public int compare(Log o1, Log o2) {
                    return o1.getTs().compareTo(o2.getTs());
                }
            });
        }
    }

    private void refresh() {
        try {
            dataModel.setData(logList);
            logTable.setModel(dataModel);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("刷新失败", e);
            JOptionPane.showMessageDialog(Context.getInstance().getMainFrame(), "可能已经删除！" + e.getMessage(), "异常", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openLogDialog() {
        int row = logTable.getSelectedRow();
        if (row > -1) {
            try {
                Log log = logList.get(row);
                String title = log.getFilename();
                String path = log.getAbsolutePath();
                String text = FileUtil.getFileToString(path);
                JDialog logDialog = new JDialog();
                JTextArea textArea = new JTextArea();
                if (text.length() > 10000) {
                    text = text.substring(text.length() - 10000, text.length());
                }
                textArea.setText(text);
                JScrollPane spane = new JScrollPane(textArea);
                logDialog.add(spane);
                int width = PropertiesUtil.toInteger(Context.getInstance().getProperty(PropertiesConstants.APP_DEFAULT_DIALOG_WIDTH));
                int height = PropertiesUtil.toInteger(Context.getInstance().getProperty(PropertiesConstants.APP_DEFAULT_DIALOG_HEIGHT));
                logDialog.setSize(width, height);
                logDialog.setTitle(title);
                logDialog.setLocationRelativeTo(Context.getInstance().getMainFrame());
                logDialog.setVisible(true);
            } catch (Exception e2) {
                e2.printStackTrace();
                logger.error(e2.getMessage());
                JOptionPane.showMessageDialog(Context.getInstance().getMainFrame(), "可能已经删除！" + e2.getMessage(), "异常", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}
