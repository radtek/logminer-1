package com.logminerplus.gui.log;

import java.io.IOException;
import java.util.Scanner;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class TextAreaLogAppender extends LogAppender {
	
	private JTextArea textArea;
	private JScrollPane scrollPane;

	public TextAreaLogAppender(JTextArea textArea, JScrollPane scrollPane) throws IOException {
		super("textArea");
		this.textArea = textArea;
		this.scrollPane = scrollPane;
	}
	
	@SuppressWarnings("resource")
	@Override
	public void run() {
		//不间断地扫描输入流  
		Scanner scanner = new Scanner(reader);
		//将扫描到的字符流输出到指定的JTextArea组件
		while (scanner.hasNextLine()) {
			try {
				//睡眠
				Thread.sleep(100);
				String line = scanner.nextLine();
				textArea.append(line);
				textArea.append("\n");
				line = null;
				//使垂直滚动条自动向下滚动 
				scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
			} catch (Exception e) {
				//异常不做处理
			}
		}
		
	}

}
