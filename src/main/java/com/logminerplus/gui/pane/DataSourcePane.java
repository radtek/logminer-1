package com.logminerplus.gui.pane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import com.logminerplus.bean.DataSource;
import com.logminerplus.jdbc.DataBase;
import com.logminerplus.utils.Context;
import com.logminerplus.utils.PropertiesConstants;

public class DataSourcePane extends BasicPane {
	
	private static Logger logger = Logger.getLogger(DataSourcePane.class.getName());
	
	private String ele;
	
	private DataSource bean;
	
	public DataSourcePane(String ele) {
		this.ele = ele;
	}
	
	//Edit States
	boolean isEdit = false;
	//Button
	private JButton initScn;
	private JButton showScn;
	private JButton resetScn;
	private JButton testBtn;
	private JButton editBtn;
	private JButton saveBtn;
	private JButton cancelBtn;
	//Field
	private JComboBox<String> selectType;
	private JTextField textDriver;
	private JTextField textAddress;
	private JTextField textPort;
	private JTextField textSID;
	private JTextField textName;
	private JTextField textUsername;
	private JTextField textPassword;
	
	public JPanel init() throws Exception {
		JPanel formPane = new JPanel();
		
		JPanel northPane = new JPanel();
		northPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		northPane.setLayout(new BoxLayout(northPane, BoxLayout.Y_AXIS));
		northPane.setPreferredSize(new Dimension(0, 280));
		
	    JPanel southPane = new JPanel();
	    southPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
	    
	    Box vBox = initFormBox();
	    northPane.add(vBox);
	    //
	    initButton();
	    
	    if("target".equals(ele)) {
	    	southPane.add(initScn);
	    	southPane.add(showScn);
	    	southPane.add(resetScn);
	    }
	    
	    southPane.add(testBtn);
	    southPane.add(editBtn);
	    southPane.add(saveBtn);
	    southPane.add(cancelBtn);
	    //
	    bean = Context.getInstance().getDataSource(ele);
	    setValue();
	    setEditStatus(false);
	    //
		formPane.setLayout(new BorderLayout(0, 0));
		formPane.add(northPane, BorderLayout.NORTH);
		formPane.add(southPane, BorderLayout.SOUTH);
		return formPane;
	}
	
	private Box initFormBox() {
		Box vBox = Box.createVerticalBox();
		
		Box b0 = Box.createHorizontalBox();
		Box b1 = Box.createHorizontalBox();
	    Box b2 = Box.createHorizontalBox();
	    Box b3 = Box.createHorizontalBox();
	    Box b4 = Box.createHorizontalBox();
	    Box b5 = Box.createHorizontalBox();
	    Box b6 = Box.createHorizontalBox();
	    Box b7 = Box.createHorizontalBox();
	    
	    String str[] = { "Oracle", "OBase"};
	    selectType = new JComboBox<String>(str);
	    selectType.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				 int index = selectType.getSelectedIndex();
				 if(index == 0) {
					 textDriver.setText("oracle.jdbc.driver.OracleDriver");
				 } else {
					 textDriver.setText("com.mysql.jdbc.Driver");
				 }
			}
		});
	    b0.add(Box.createHorizontalStrut(10));
	    b0.add(new JLabel(Context.getInstance().getProperty(PropertiesConstants.LABEL_DATASOURCE_TYPE)));
	    b0.add(Box.createHorizontalStrut(10));
	    b0.add(selectType);
	    b0.add(new JLabel("*"));
	    
	    textDriver = new JTextField();
	    textDriver.setEditable(false);
	    b1.add(Box.createHorizontalStrut(10));
	    b1.add(new JLabel(Context.getInstance().getProperty(PropertiesConstants.LABEL_DATASOURCE_DRIVER)));
	    b1.add(Box.createHorizontalStrut(10));
	    b1.add(textDriver);
	    b1.add(new JLabel("*"));
	    
	    textAddress = new JTextField();
	    textAddress.setText("127.0.0.1");
	    b2.add(Box.createHorizontalStrut(10));
	    b2.add(new JLabel(Context.getInstance().getProperty(PropertiesConstants.LABEL_DATASOURCE_ADDRESS)));  
	    b2.add(Box.createHorizontalStrut(10));  
	    b2.add(textAddress);
	    b2.add(new JLabel("*"));
	    
	    textPort = new JTextField();
	    textPort.addKeyListener(new KeyAdapter(){
	    	public void keyTyped(KeyEvent e) {
	    		int keyChar = e.getKeyChar();              
                if(keyChar >= KeyEvent.VK_0 && keyChar <= KeyEvent.VK_9){
                
                } else{
                	//关键，屏蔽掉非法输入
                    e.consume();
                }
            }
        });
	    b3.add(Box.createHorizontalStrut(10));
	    b3.add(new JLabel(Context.getInstance().getProperty(PropertiesConstants.LABEL_DATASOURCE_PORT)));
	    b3.add(Box.createHorizontalStrut(10));  
	    b3.add(textPort);
	    b3.add(new JLabel("*"));
	    
	    textSID = new JTextField();
	    b4.add(Box.createHorizontalStrut(10));
	    b4.add(new JLabel(Context.getInstance().getProperty(PropertiesConstants.LABEL_DATASOURCE_SID)));  
	    b4.add(Box.createHorizontalStrut(10));  
	    b4.add(textSID);
	    b4.add(new JLabel("*"));
	    
	    textName = new JTextField();
	    b5.add(Box.createHorizontalStrut(10));
	    b5.add(new JLabel(Context.getInstance().getProperty(PropertiesConstants.LABEL_DATASOURCE_NAME)));  
	    b5.add(Box.createHorizontalStrut(10));  
	    b5.add(textName);
	    b5.add(new JLabel("*"));
	    
	    textUsername = new JTextField();
	    b6.add(Box.createHorizontalStrut(10));
	    b6.add(new JLabel(Context.getInstance().getProperty(PropertiesConstants.LABEL_DATASOURCE_USERNAME)));  
	    b6.add(Box.createHorizontalStrut(10));  
	    b6.add(textUsername);
	    b6.add(new JLabel("*"));
	    
	    textPassword = new JPasswordField();
	    b7.add(Box.createHorizontalStrut(10));
	    b7.add(new JLabel(Context.getInstance().getProperty(PropertiesConstants.LABEL_DATASOURCE_PASSWORD)));  
	    b7.add(Box.createHorizontalStrut(10));  
	    b7.add(textPassword);
	    b7.add(new JLabel("*"));
	    
	    vBox.add(b0);
	    vBox.add(Box.createVerticalStrut(10));
	    vBox.add(b1);
	    vBox.add(Box.createVerticalStrut(10));
	    vBox.add(b2);
	    vBox.add(Box.createVerticalStrut(10));
	    vBox.add(b3);
	    vBox.add(Box.createVerticalStrut(10));
	    vBox.add(b4);
	    vBox.add(Box.createVerticalStrut(10));
	    vBox.add(b5);
	    vBox.add(Box.createVerticalStrut(10));
	    vBox.add(b6);
	    vBox.add(Box.createVerticalStrut(10));
	    vBox.add(b7);
	    vBox.add(Box.createVerticalStrut(10));
	    
	    return vBox;
	}
	
	private void initButton() {
		initScn = new JButton(Context.getInstance().getProperty(PropertiesConstants.BUTTON_SCN_INIT));
		initScn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				initLastScn();
				logger.info("初始化成功！\nLAST SCN: "+0);
				JOptionPane.showMessageDialog(Context.getInstance().getMainFrame(), "初始化成功！\nLAST SCN: "+0, "提示", JOptionPane.INFORMATION_MESSAGE);
			};
		});
		showScn = new JButton(Context.getInstance().getProperty(PropertiesConstants.BUTTON_SCN_SHOW));
		showScn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String scn[] = getLastScn();
				String lastscn = scn[0];
				String lasttime = scn[1];
				JOptionPane.showMessageDialog(Context.getInstance().getMainFrame(), "LAST SCN : "+lastscn+"\nLAST TIME: "+lasttime, "提示", JOptionPane.INFORMATION_MESSAGE);
			};
		});
		resetScn = new JButton(Context.getInstance().getProperty(PropertiesConstants.BUTTON_SCN_RESET));
		resetScn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String scn[] = getLastScn();
				String lastScn =JOptionPane.showInputDialog(Context.getInstance().getMainFrame(), "LAST SCN:", scn[0]); 
				if(lastScn!=null) {
					setLastScn(lastScn);
					logger.info("更新成功！\nLAST SCN: "+lastScn);
					JOptionPane.showMessageDialog(Context.getInstance().getMainFrame(), "更新成功！\nLAST SCN: "+lastScn, "提示", JOptionPane.INFORMATION_MESSAGE);
				}
			};
		});
		testBtn = new JButton(Context.getInstance().getProperty(PropertiesConstants.BUTTON_TEST));
		testBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Connection conn = null;
				try {
					conn = DataBase.getConnection(ele);
					logger.info("连接成功！-"+ele);
					JOptionPane.showMessageDialog(Context.getInstance().getMainFrame(), "连接成功！", "提示", JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e2) {
					e2.printStackTrace();
					logger.debug("连接失败", e2);
					JOptionPane.showMessageDialog(Context.getInstance().getMainFrame(), "连接失败："+e2.getMessage(), "异常", JOptionPane.ERROR_MESSAGE);
				} finally {
					if(conn!=null) {
						try {
							conn.close();
						} catch (SQLException e1) {
							e1.printStackTrace();
							logger.error("关连失败", e1);
						}
					}
				}
				
			}
		});
	    editBtn = new JButton(Context.getInstance().getProperty(PropertiesConstants.BUTTON_EDIT));
	    editBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEditStatus(true);
			}
		});
	    saveBtn = new JButton(Context.getInstance().getProperty(PropertiesConstants.BUTTON_SAVE));
	    saveBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String prefix = Context.getInstance().getProperty(PropertiesConstants.MENU_LIST_DATASOURCE);
				try {
					DataSource bean = getValue(ele);
					Context.getInstance().writeDataSource(ele, bean);
					logger.info(prefix+"-保存成功！-" + ele);
				} catch (Exception e2) {
					e2.printStackTrace();
					logger.error(prefix+"-保存失败", e2);
					JOptionPane.showMessageDialog(Context.getInstance().getMainFrame(), "保存失败！"+e2.getMessage(), "异常", JOptionPane.ERROR_MESSAGE);
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
		JComponent[] forms = {selectType, 
					    		textAddress,
					    		textPort,
					    		textSID,
					    		textName,
					    		textUsername,
					    		textPassword};
		loadComponentStatus(forms, isEdit);
	    loadComponentStatus(new JComponent[]{saveBtn, cancelBtn}, isEdit);
	    loadComponentStatus(new JComponent[]{initScn, showScn, resetScn, testBtn, editBtn}, !isEdit);
	}
	
	private void setValue() {
		selectType.setSelectedItem(bean.getType());
		textDriver.setText(bean.getDriver());
		textAddress.setText(bean.getAddress());
		textPort.setText(bean.getPort()+"");
		textSID.setText(bean.getSid());
		textName.setText(bean.getName());
		textUsername.setText(bean.getUsername());
		textPassword.setText(bean.getPassword());
	}
	
	private DataSource getValue(String ele) throws Exception {
		bean = new DataSource();
		bean.setType(selectType.getSelectedItem().toString());
		bean.setDriver(textDriver.getText());
		bean.setAddress(textAddress.getText());
		bean.setPort(Integer.parseInt(textPort.getText()));
		bean.setSid(textSID.getText());
		bean.setName(textName.getText());
		bean.setUsername(textUsername.getText());
		bean.setPassword(textPassword.getText());
		return bean;
	}
	
	private void initLastScn() {
		Connection conn = null;
		try {
			conn = DataBase.getConnection(ele);
			Statement stat = conn.createStatement();
			stat.executeUpdate("drop table if exists scn");
			stat.executeUpdate("create table scn(lastscn VARCHAR(100) not null, lasttime timestamp, primary key(lastscn))");
			stat.executeUpdate("insert into scn(lastscn, lasttime) values(0, current_timestamp())");
		} catch (Exception e2) {
			e2.printStackTrace();
			logger.debug("查询失败", e2);
			JOptionPane.showMessageDialog(Context.getInstance().getMainFrame(), "查询失败："+e2.getMessage(), "异常", JOptionPane.ERROR_MESSAGE);
		} finally {
			if(conn!=null) {
				try {
					conn.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
					logger.error("关连失败", e1);
				}
			}
		}
	}
	
	private String[] getLastScn() {
		String scn[] = new String[2];
		Connection conn = null;
		try {
			conn = DataBase.getConnection(ele);
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("select * from scn limit 0, 1");
			while(rs.next()) {
				scn[0] = rs.getString(1);
				scn[1] = rs.getString(2);
			}
		} catch (Exception e2) {
			e2.printStackTrace();
			logger.debug("查询失败", e2);
			JOptionPane.showMessageDialog(Context.getInstance().getMainFrame(), "查询失败："+e2.getMessage(), "异常", JOptionPane.ERROR_MESSAGE);
		} finally {
			if(conn!=null) {
				try {
					conn.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
					logger.error("关连失败", e1);
				}
			}
		}
		return scn;
	}
	
	
	private void setLastScn(String lastScn) {
		Connection conn = null;
		try {
			conn = DataBase.getConnection(ele);
			Statement stat = conn.createStatement();
			stat.executeUpdate("update scn set lastscn='"+lastScn+"', lasttime=current_timestamp()");
		} catch (Exception e2) {
			e2.printStackTrace();
			logger.debug("查询失败", e2);
			JOptionPane.showMessageDialog(Context.getInstance().getMainFrame(), "查询失败："+e2.getMessage(), "异常", JOptionPane.ERROR_MESSAGE);
		} finally {
			if(conn!=null) {
				try {
					conn.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
					logger.error("关连失败", e1);
				}
			}
		}
	}
	
}
