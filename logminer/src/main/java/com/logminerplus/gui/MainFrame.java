package com.logminerplus.gui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import com.logminerplus.gui.pane.ConsolePane;
import com.logminerplus.gui.pane.DataSourcePane;
import com.logminerplus.gui.pane.FieldMapperPane;
import com.logminerplus.gui.pane.FileDirPane;
import com.logminerplus.gui.pane.HomePane;
import com.logminerplus.gui.pane.ListenerManagePane;
import com.logminerplus.gui.pane.LogManagePane;
import com.logminerplus.utils.Context;
import com.logminerplus.utils.ContextConstants;
import com.logminerplus.utils.PropertiesConstants;
import com.logminerplus.utils.PropertiesUtil;

public class MainFrame extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1271790440723642650L;
	
	private static Logger logger = Logger.getLogger(MainFrame.class.getName());
	
	//菜单条
	private MenuBar menuBar;
	//内容面板
	private JPanel contentPane;
	//树形菜单
	private JList<String> menuList;
	private List<String> menuNameList;
	//选项卡内容
	private JTabbedPane mainTabbedPane;
	
	public MainFrame() {
		logger.info("enter MainFrame()");
		long initStart = System.currentTimeMillis();
		try {
			initContext();
			logger.info("Initialization Starting ********");
			initContent();
			initTray();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("初始化异常", e);
		}
		logger.info("******** Welcome ********");
		logger.info("******** Welcome ********");
		logger.info("******** Welcome ********");
		logger.info("******** Welcome ********");
		logger.info("******** Welcome ********");
		logger.info("******** Welcome ********");
		logger.info("******** Welcome ********");
		logger.info("******** Welcome ********");
		long initEnd = System.currentTimeMillis();
		long millisecond = initEnd-initStart;
		logger.info("Initialization processed in "+millisecond+" ms");
	}
	
	private void initContext() throws Exception {
		//读取语言配置文件
		logger.info("----------------------------------------------------");
		String localeFile = ContextConstants.CONFIG_LOCALE_ZH;
		String osName = System.getProperty("os.name");
		if(!osName.startsWith("Win")) {
			localeFile = ContextConstants.CONFIG_LOCALE_EN;
		}
		logger.info("OS:"+osName);
		Properties propertiesConstants = PropertiesUtil.loadProp(localeFile);
		//
		Context context = Context.getInstance();
		context.setPropertiesConstants(propertiesConstants);
		context.setMainFrame(this);
	}
	
	private void initContent() throws Exception {
		//基础信息设置
		setTitle(Context.getInstance().getProperty(PropertiesConstants.APP_MAIN_TITLE));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		int width = PropertiesUtil.toInteger(Context.getInstance().getProperty(PropertiesConstants.APP_MAIN_WIDTH));
		int height = PropertiesUtil.toInteger(Context.getInstance().getProperty(PropertiesConstants.APP_MAIN_HEIGHT));
		setSize(width, height);
		//屏幕显示居中
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = this.getSize();
        this.setLocation( (screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        //设置菜单条
        setMenuBar(loadMenuBar());
        //设置主内容
		setContentPane(contentPane());
	}
	
	/**
	 * 初始化系统托盘
	 */
	private void initTray() {
		// 图标
		Image icon16 = Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource("images/icon16×16.png"));
		Image icon32 = Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource("images/icon32×32.png"));
		// 是否支持托盘
		if (SystemTray.isSupported()) {
			PopupMenu popup = new PopupMenu();
			MenuItem show = new MenuItem(Context.getInstance().getProperty(PropertiesConstants.MENU_ITEM_SHOW));
			MenuItem exit = new MenuItem(Context.getInstance().getProperty(PropertiesConstants.MENU_ITEM_EXIT));
			popup.add(show);
			popup.add(exit);
			// 托盘图标
			TrayIcon trayIcon = new TrayIcon(icon16, Context.getInstance().getProperty(PropertiesConstants.APP_MAIN_TITLE), popup);
			// 获得系统托盘
			SystemTray systemTray = SystemTray.getSystemTray();
			try {
				// 设置托盘的图标
				systemTray.add(trayIcon);
			} catch (AWTException e) {
				e.printStackTrace();
			}
			addWindowListener(new WindowListener() {
				
				public void windowOpened(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				public void windowIconified(WindowEvent e) {
					dispose();// 窗口最小化时dispose该窗口
				}
				
				public void windowDeiconified(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				public void windowDeactivated(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				public void windowClosing(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				public void windowClosed(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				public void windowActivated(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}
			});
			trayIcon.addMouseListener(new MouseListener() {
				
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
					// 左击击托盘窗口再现，如果是双击就是e.getClickCount() == 2
					if (e.getClickCount() == 1 && e.getButton() != MouseEvent.BUTTON3) {
						setVisible(true);
						setExtendedState(JFrame.NORMAL);//设置此 frame 的状态。
					}
				}
			});
			// 点击[显示窗口]菜单后将窗口显示出来
			show.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent e) {
					// 从系统的托盘实例中移除托盘图标
					// systemTray.remove(trayIcon); 
					setVisible(true);
					setExtendedState(JFrame.NORMAL);
				}
			});
			// 点击[退出]菜单后推出程序
			exit.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent e) {
					// 退出时的操作
					// 退出程序
					logger.debug("退出程序******");
					System.exit(0);
				}
			});
			// 设置程序图标
			setIconImage(icon32);
		}
	}
	
	/**
	 * 内容面板
	 * @return
	 */
	private JPanel contentPane() throws Exception {
		if(contentPane==null) {
			contentPane = new JPanel();
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			contentPane.setLayout(new BorderLayout(0, 0));
			//左边树形菜单，右边选项卡
			contentPane.add(loadListMenu(), BorderLayout.WEST);
			contentPane.add(loadContentTabbedPane(), BorderLayout.CENTER);
		}
		return contentPane;
	}
	
	/**
	 * 加载菜单
	 * @return
	 */
	private MenuBar loadMenuBar() {
		if(menuBar==null) {
			//菜单
	        Menu menuFile = new Menu(Context.getInstance().getProperty(PropertiesConstants.MENU_FILE));
	        MenuItem mItemHide = new MenuItem(Context.getInstance().getProperty(PropertiesConstants.MENU_FILE_HIDE));
	        mItemHide.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(contentPane, Context.getInstance().getProperty(PropertiesConstants.MENU_FILE_HIDE), "提示", JOptionPane.WARNING_MESSAGE);
				}
			});
	        MenuItem mItemExit = new MenuItem(Context.getInstance().getProperty(PropertiesConstants.MENU_FILE_EXIT));
	        mItemExit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					logger.info("********Exit********");
					System.exit(0);
				}
			});
	        menuFile.add(mItemHide);
	        menuFile.addSeparator();
	        menuFile.add(mItemExit);
	        
			//日志
	        Menu menuLog = new Menu(Context.getInstance().getProperty(PropertiesConstants.MENU_LOG));
	        MenuItem menuLogView = new MenuItem(Context.getInstance().getProperty(PropertiesConstants.MENU_LOG_VIEW));
	        menuLogView.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(menuNameList!=null) {
						int index = menuNameList.indexOf(Context.getInstance().getProperty(PropertiesConstants.MENU_LIST_LOGMANAGER));
						if(index>-1) menuList.setSelectedIndex(index);
					}
				}
			});
	        MenuItem menuLogImport = new MenuItem(Context.getInstance().getProperty(PropertiesConstants.MENU_LOG_EXPORT));
	        menuLogImport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(contentPane, "暂不支持", "提示", JOptionPane.WARNING_MESSAGE);
				}
			});
	        menuLog.add(menuLogView);
	        //menuLog.addSeparator();
	        //menuLog.add(menuLogImport);
	        
	        //帮助
	        Menu menuHelp = new Menu(Context.getInstance().getProperty(PropertiesConstants.MENU_HELP));
	        MenuItem menuHelpHelp = new MenuItem(Context.getInstance().getProperty(PropertiesConstants.MENU_HELP));
	        menuHelpHelp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(contentPane, Context.getInstance().getProperty(PropertiesConstants.APP_MAIN_TITLE), "提示", JOptionPane.INFORMATION_MESSAGE);
				}
			});
	        MenuItem menuHelpAbout = new MenuItem(Context.getInstance().getProperty(PropertiesConstants.MENU_HELP_ABOUT));
	        menuHelpAbout.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(contentPane, Context.getInstance().getProperty(PropertiesConstants.APP_MAIN_TITLE), "提示", JOptionPane.INFORMATION_MESSAGE);
				}
			});
	        menuHelp.add(menuHelpHelp);
	        menuHelp.addSeparator();
	        menuHelp.add(menuHelpAbout);
	        
	        //添加按钮组
	        menuBar = new MenuBar();
	        menuBar.add(menuFile);
	        menuBar.add(menuLog);
	        menuBar.add(menuHelp);
		}
        return menuBar;
	}
	
	private JScrollPane loadListMenu() {
		if(menuNameList==null) {
			menuNameList = new ArrayList<String>();
			menuNameList.add(Context.getInstance().getProperty(PropertiesConstants.MENU_LIST_HOME));
			//menuNameList.add(Context.getInstance().getProperty(PropertiesConstants.MENU_LIST_SETTINGS));
			menuNameList.add(Context.getInstance().getProperty(PropertiesConstants.MENU_LIST_DATASOURCE));
			menuNameList.add(Context.getInstance().getProperty(PropertiesConstants.MENU_LIST_FILEDIR));
			menuNameList.add(Context.getInstance().getProperty(PropertiesConstants.MENU_LIST_FIELDMAP));
			menuNameList.add(Context.getInstance().getProperty(PropertiesConstants.MENU_LIST_LISTENER));
			menuNameList.add(Context.getInstance().getProperty(PropertiesConstants.MENU_LIST_LOGMANAGER));
			//menuNameList.add(Context.getInstance().getProperty(PropertiesConstants.MENU_LIST_CONSOLE));
		}
		JScrollPane scrollPane = new JScrollPane();
		if(menuList==null) {
			menuList = new JList<String>();
			menuList.setPreferredSize(new Dimension(160, 0));
			DefaultListModel<String> listModel = new DefaultListModel<String>();
			if(menuNameList!=null&&!menuNameList.isEmpty()) {
				for (int i = 0; i < menuNameList.size(); i++) {
					listModel.addElement(menuNameList.get(i));
				}
			}
			menuList.setModel(listModel);
			menuList.addListSelectionListener(new ListSelectionListener() {
				@SuppressWarnings("unchecked")
				public void valueChanged(ListSelectionEvent e) {
					JList<String> list = (JList<String>)e.getSource();
					int index = list.getSelectedIndex();
					mainTabbedPane.setSelectedIndex(index);
				}
			});
		}
		scrollPane.setViewportView(menuList);
		return scrollPane;
	}
	
	private JTabbedPane loadContentTabbedPane() throws Exception {
		if(mainTabbedPane==null) {
			mainTabbedPane = new JTabbedPane();
			logger.debug("Init Home Start");
			mainTabbedPane.add(">>", new HomePane().init());
			logger.debug("Init Home End");
			//logger.debug("Init Settings Start");
			//mainTabbedPane.add(">>", new SettingsPane().init());
			//logger.debug("Init Settings End");
			logger.debug("Init DS Start");
			mainTabbedPane.add(">>", createDSPane());
			logger.debug("Init DS End");
			logger.debug("Init FileDir Start");
			mainTabbedPane.add(">>", new FileDirPane().init());
			logger.debug("Init FileDir End");
			logger.debug("Init FieldMapper Start");
			mainTabbedPane.add(">>", new FieldMapperPane().init());
			logger.debug("Init FieldMapper End");
			logger.debug("Init ListenerManage Start");
			mainTabbedPane.add(">>", new ListenerManagePane().init());
			logger.debug("Init ListenerManage End");
			logger.debug("Init LogManage Start");
			mainTabbedPane.add(">>", new LogManagePane().init());
			logger.debug("Init LogManage End");
			//logger.debug("Init Console Start");
			//mainTabbedPane.add(">>", new ConsolePane().init());
			//logger.debug("Init Console End");
			mainTabbedPane.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					JTabbedPane jtp = (JTabbedPane) e.getSource();
					int index = jtp.getSelectedIndex();
					menuList.setSelectedIndex(index);
				}
			});
		}
	    return mainTabbedPane;
	}
	
	public JTabbedPane createDSPane() throws Exception {
		JTabbedPane  dbTabbedPane = new JTabbedPane();
		dbTabbedPane.add(Context.getInstance().getProperty(PropertiesConstants.TABBEDPANE_DATASOURCE_SOURCE), new DataSourcePane("source").init());
		dbTabbedPane.add(Context.getInstance().getProperty(PropertiesConstants.TABBEDPANE_DATASOURCE_TARGET), new DataSourcePane("target").init());
		return dbTabbedPane;
	}
	
}
