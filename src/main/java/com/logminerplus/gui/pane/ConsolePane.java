package com.logminerplus.gui.pane;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import com.logminerplus.gui.log.TextAreaLogAppender;
import com.logminerplus.utils.Context;
import com.logminerplus.utils.PropertiesConstants;

public class ConsolePane {
	
	private static Logger logger = Logger.getLogger(ConsolePane.class.getName());
	
	private JTextArea textArea;
	private JScrollPane scrollPane;
	
	
	public JTabbedPane init() throws Exception {
		JPanel mainPanel = new JPanel();
		
		textArea = new JTextArea();
		scrollPane = new JScrollPane(textArea);
	    
	    JPanel southPane = new JPanel();
	    southPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
	    
	    JButton refreshBtn = new JButton(Context.getInstance().getProperty(PropertiesConstants.BUTTON_CLEAR));
	    refreshBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea.setText("");
			}
		});
	    
	    initLog();
	    
	    southPane.add(refreshBtn);
	    
		mainPanel.setLayout(new BorderLayout(0, 0));
		mainPanel.add(scrollPane, BorderLayout.CENTER);
		mainPanel.add(southPane, BorderLayout.SOUTH);
		
		JTabbedPane  tabbedPane = new JTabbedPane();
		tabbedPane.add(Context.getInstance().getProperty(PropertiesConstants.TABBEDPANE_CONSOLE), mainPanel);
		return tabbedPane;
	}
	
	public void initLog() {
		try {
			Thread thread = new TextAreaLogAppender(textArea, scrollPane);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("绑定输出异常！", e);
		}
	}
	
}
