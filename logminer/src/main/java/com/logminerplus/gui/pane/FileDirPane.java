package com.logminerplus.gui.pane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;

import org.apache.log4j.Logger;

import com.logminerplus.bean.Fileset;
import com.logminerplus.utils.Context;
import com.logminerplus.utils.PropertiesConstants;

public class FileDirPane extends BasicPane {
	
	private static Logger logger = Logger.getLogger(FileDirPane.class.getName());
	
	private Fileset bean;
	//Edit States
	boolean isEdit = false;
	//Button
	private JButton editBtn;
	private JButton saveBtn;
	private JButton cancelBtn;
	//
	private JTextField dictDirField;
	private JButton dictSelectBtn;
	private JTextField logDirField;
	private JButton logDirSelectBtn;
	private JTextField watcherDirField;
	private JButton watcherDirSelectBtn;
	private JTextField bakDirField;
	private JButton bakDirSelectBtn;
	
	public JTabbedPane init() throws Exception {
		JPanel mainPanel = new JPanel();
	    
	    JPanel northPane = new JPanel();
		northPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		northPane.setLayout(new BoxLayout(northPane, BoxLayout.Y_AXIS));
		northPane.setPreferredSize(new Dimension(0, 150));
	    
		Box vBox = Box.createVerticalBox();
		
		Box b0 = Box.createHorizontalBox();
		Box b1 = Box.createHorizontalBox();
		Box b2 = Box.createHorizontalBox();
	    Box b3 = Box.createHorizontalBox();
	    
	    dictDirField = new JTextField();
	    dictSelectBtn = new JButton(Context.getInstance().getProperty(PropertiesConstants.BUTTON_SELECT));
	    b0.add(Box.createHorizontalStrut(10));
	    b0.add(new JLabel(Context.getInstance().getProperty(PropertiesConstants.BUTTON_DIR_DICT)));
	    b0.add(Box.createHorizontalStrut(10));
	    b0.add(dictDirField);
	    b0.add(Box.createHorizontalStrut(10));
	    b0.add(dictSelectBtn);
	    b0.add(Box.createHorizontalStrut(10));
	    
	    logDirField = new JTextField();
	    logDirSelectBtn = new JButton(Context.getInstance().getProperty(PropertiesConstants.BUTTON_SELECT));
	    b1.add(Box.createHorizontalStrut(10));
	    b1.add(new JLabel(Context.getInstance().getProperty(PropertiesConstants.BUTTON_DIR_LOG)));
	    b1.add(Box.createHorizontalStrut(10));
	    b1.add(logDirField);
	    b1.add(Box.createHorizontalStrut(10));
	    b1.add(logDirSelectBtn);
	    b1.add(Box.createHorizontalStrut(10));
	    
	    watcherDirField = new JTextField();
	    watcherDirSelectBtn = new JButton(Context.getInstance().getProperty(PropertiesConstants.BUTTON_SELECT));
	    b2.add(Box.createHorizontalStrut(10));
	    b2.add(new JLabel(Context.getInstance().getProperty(PropertiesConstants.BUTTON_DIR_WATCHER)));
	    b2.add(Box.createHorizontalStrut(10));
	    b2.add(watcherDirField);
	    b2.add(Box.createHorizontalStrut(10));
	    b2.add(watcherDirSelectBtn);
	    b2.add(Box.createHorizontalStrut(10));
	    
	    bakDirField = new JTextField();
	    bakDirSelectBtn = new JButton(Context.getInstance().getProperty(PropertiesConstants.BUTTON_SELECT));
	    b3.add(Box.createHorizontalStrut(10));
	    b3.add(new JLabel(Context.getInstance().getProperty(PropertiesConstants.BUTTON_DIR_BAK)));
	    b3.add(Box.createHorizontalStrut(10));  
	    b3.add(bakDirField);
	    b3.add(Box.createHorizontalStrut(10));
	    b3.add(bakDirSelectBtn);
	    b3.add(Box.createHorizontalStrut(10));
	    
	    vBox.add(b0);
	    vBox.add(Box.createVerticalStrut(10));
	    vBox.add(b1);
	    vBox.add(Box.createVerticalStrut(10));
	    vBox.add(b2);
	    vBox.add(Box.createVerticalStrut(10));
	    vBox.add(b3);
	    
	    northPane.add(vBox);
	    
	    JPanel southPane = new JPanel();
	    southPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
	    //
	    initButton();
	    southPane.add(editBtn);
	    southPane.add(saveBtn);
	    southPane.add(cancelBtn);
	    //
	    bean = Context.getInstance().getFileSet();
	    setValue();
	    setEditStatus(false);
	    //
		mainPanel.setLayout(new BorderLayout(0, 0));
		mainPanel.add(northPane, BorderLayout.NORTH);
		mainPanel.add(southPane, BorderLayout.SOUTH);
		
		JTabbedPane  dbTabbedPane = new JTabbedPane();
		dbTabbedPane.add(Context.getInstance().getProperty(PropertiesConstants.TABBEDPANE_FILEDIR), mainPanel);
		return dbTabbedPane;
	}
	
	private void initButton() {
		dictSelectBtn.addActionListener(new SelectActionListener(JFileChooser.DIRECTORIES_ONLY, dictDirField));
		logDirSelectBtn.addActionListener(new SelectActionListener(JFileChooser.DIRECTORIES_ONLY, logDirField));
		bakDirSelectBtn.addActionListener(new SelectActionListener(JFileChooser.DIRECTORIES_ONLY, bakDirField));
		watcherDirSelectBtn.addActionListener(new SelectActionListener(JFileChooser.DIRECTORIES_ONLY, watcherDirField));
		editBtn = new JButton(Context.getInstance().getProperty(PropertiesConstants.BUTTON_EDIT));
	    editBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEditStatus(true);
			}
		});
	    saveBtn = new JButton(Context.getInstance().getProperty(PropertiesConstants.BUTTON_SAVE));
	    saveBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String prefix = Context.getInstance().getProperty(PropertiesConstants.MENU_LIST_FILEDIR);
				try {
					Fileset bean = getValue();
					Context.getInstance().writeFileSet(bean);
					logger.info(prefix+"-保存成功！");
				} catch (Exception e2) {
					e2.printStackTrace();
					logger.error(prefix+"-保存失败", e2);
				}
				setEditStatus(false);
			}
		});
	    cancelBtn = new JButton(Context.getInstance().getProperty(PropertiesConstants.BUTTON_CANCEL));
	    cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setValue();
				setEditStatus(false);
			}
		});
	}

	@Override
	public void setEditStatus(boolean isEdit) {
		this.isEdit = isEdit;
		JComponent[] forms = {dictDirField, 
								dictSelectBtn,
					    		logDirField,
					    		logDirSelectBtn,
					    		watcherDirField,
					    		watcherDirSelectBtn,
					    		bakDirField,
					    		bakDirSelectBtn};
		loadComponentStatus(forms, isEdit);
	    loadComponentStatus(new JComponent[]{saveBtn, cancelBtn}, isEdit);
	    loadComponentStatus(new JComponent[]{editBtn}, !isEdit);
	}
	
	class SelectActionListener implements ActionListener {
		private int mode;
		private JTextField field;
		public SelectActionListener(int mode, JTextField field) {
			this.mode = mode;
			this.field = field;
		}
		public void actionPerformed(ActionEvent e) {
			JFileChooser jfc = new JFileChooser();
			FileSystemView fsv = FileSystemView.getFileSystemView();
			jfc.setDialogTitle("请选择");
			jfc.setFileSelectionMode(mode);
			jfc.setCurrentDirectory(fsv.getHomeDirectory());
			if(field.getText()!=null&&!"".equals(field.getText())) {
				jfc.setCurrentDirectory(new File(field.getText()));
			}
			int result = jfc.showOpenDialog(Context.getInstance().getMainFrame());
			if(JFileChooser.APPROVE_OPTION == result) {
				File file = jfc.getSelectedFile();
				String path = file.getAbsolutePath();
				field.setText(path);
			}
		}
	}
	
	private void setValue() {
		dictDirField.setText(bean.getDictdir());
		logDirField.setText(bean.getLogdir());
		watcherDirField.setText(bean.getWatcherdir());
		bakDirField.setText(bean.getBakdir());
	}
	
	private Fileset getValue() throws Exception {
		bean = new Fileset();
		bean.setDictdir(dictDirField.getText());
		bean.setLogdir(logDirField.getText());
		bean.setWatcherdir(watcherDirField.getText());
		bean.setBakdir(bakDirField.getText());
		return bean;
	}
	
}
