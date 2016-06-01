package com.logminerplus.gui.pane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;

import com.logminerplus.bean.Mapper;
import com.logminerplus.utils.Context;
import com.logminerplus.utils.PropertiesConstants;

public class FieldMapperPane extends BasicPane {
	
	private static Logger logger = Logger.getLogger(FieldMapperPane.class.getName());
	
	private List<Mapper> mapList = new ArrayList<Mapper>();
	
	//Edit States
	boolean isEdit = false;
	//Button
	private JButton editBtn;
	private JButton addLineBtn;
	private JButton delLineBtn;
	private JButton saveBtn;
	private JButton cancelBtn;
	//
	private JTextField sourceField;
	private JTextField targetField;
	//
	private JTable table;
	private DefaultTableModel dataModel;
	private Vector<String> columnNames;
	private Vector<Vector<String>> rowData;
	
	public JTabbedPane init() throws Exception {
		JPanel mainPanel = new JPanel();
	    
	    JPanel northPane = new JPanel();
		northPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		northPane.setLayout(new BoxLayout(northPane, BoxLayout.Y_AXIS));
		northPane.setPreferredSize(new Dimension(0, 50));
		
		Box vBox = Box.createVerticalBox();
		Box b1 = Box.createHorizontalBox();
		
	    sourceField = new JTextField();
	    targetField = new JTextField();
	    b1.add(Box.createHorizontalStrut(10));
	    b1.add(sourceField);
	    b1.add(new JLabel(">>"));
	    b1.add(targetField);
	    b1.add(Box.createHorizontalStrut(10));
	    
	    vBox.add(Box.createVerticalStrut(5));
	    vBox.add(b1);
	    vBox.add(Box.createVerticalStrut(5));
	    
//	    northPane.add(vBox);
	    
	    JPanel centerPane = new JPanel();
	    centerPane.setLayout(new BorderLayout(0, 0));
	    
	    initData();
	    initTable();
	    
		JScrollPane logScrollPane = new JScrollPane(table);
		centerPane.add(logScrollPane, BorderLayout.CENTER);
	    
	    JPanel southPane = new JPanel();
	    northPane.setBorder(new EmptyBorder(5, 5, 5, 5));
	    southPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
	    
	    initButton();
	    setEditStatus(false);
	    
	    southPane.add(editBtn);
	    southPane.add(addLineBtn);
	    southPane.add(delLineBtn);
	    southPane.add(saveBtn);
	    southPane.add(cancelBtn);
	    
		mainPanel.setLayout(new BorderLayout(0, 0));
//		mainPanel.add(northPane, BorderLayout.NORTH);
		mainPanel.add(centerPane, BorderLayout.CENTER);
		mainPanel.add(southPane, BorderLayout.SOUTH);
		
		JTabbedPane  dbTabbedPane = new JTabbedPane();
		dbTabbedPane.add(Context.getInstance().getProperty(PropertiesConstants.TABBEDPANE_FIELDMAPPER), mainPanel);
		return dbTabbedPane;
	}
	
	private void initTable() {
		dataModel = new DefaultTableModel(rowData, columnNames){
			private static final long serialVersionUID = 7469294117927470998L;
			@Override
			public boolean isCellEditable(int row, int column) {
				return super.isCellEditable(row, column);
			}
		};
		table = new JTable();
		table.setRowHeight(Context.getInstance().getPropertyToInteger(PropertiesConstants.TABLE_ROWHEIGHT));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setModel(dataModel);
		RowSorter<DefaultTableModel> sorter = new TableRowSorter<DefaultTableModel>(dataModel);
		table.setRowSorter(sorter);
		table.setRowSelectionInterval(0, 0);
	}
	
	private void initButton() {
		addLineBtn = new JButton(Context.getInstance().getProperty(PropertiesConstants.BUTTON_ADDLINE));
		addLineBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//在选择行下插入行
				int sr =table.getSelectedRow();
				dataModel.insertRow(sr+1, new Vector<String>());
				//选中插入行
				table.setRowSelectionInterval(0, sr+1);
			}
		});
		delLineBtn = new JButton(Context.getInstance().getProperty(PropertiesConstants.BUTTON_DELLINE));
		delLineBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				if(row!=-1) {
					dataModel.removeRow(row);
					if(row==0&&dataModel.getRowCount()>0)
						table.setRowSelectionInterval(0, 0);
					if(row>0)
						table.setRowSelectionInterval(0, row-1);
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
				try {
					if(rowData!=null&&rowData.size()>0) {
						List<Mapper> mList = new ArrayList<Mapper>();
						for (int i = 0; i < rowData.size(); i++) {
							Vector<String> vc = rowData.get(i);
							if(vc.get(0)==null||"".equals(vc.get(0))||vc.get(1)==null||"".equals(vc.get(1))||vc.get(2)==null||"".equals(vc.get(2))) {
								JOptionPane.showMessageDialog(Context.getInstance().getMainFrame(), "不能有空数据！", "警告", JOptionPane.ERROR_MESSAGE);
								return;
							}
							Mapper mapper = new Mapper();
							mapper.setTablename(vc.get(0).toUpperCase());
							mapper.setSource(vc.get(1).toUpperCase());
							mapper.setTarget(vc.get(2).toUpperCase());
							mList.add(mapper);
						}
						String prefix = Context.getInstance().getProperty(PropertiesConstants.MENU_LIST_FIELDMAP);
						Context.getInstance().writeMapper("field", mList);
						mapList = mList;
						logger.info(prefix+"-保存成功！");
					}
				} catch (Exception e1) {
					e1.printStackTrace();
					logger.error("-保存失败", e1);
					JOptionPane.showMessageDialog(Context.getInstance().getMainFrame(), "保存失败！"+e1.getMessage(), "异常", JOptionPane.ERROR_MESSAGE);
				}
				setEditStatus(false);
			}
		});
	    cancelBtn = new JButton(Context.getInstance().getProperty(PropertiesConstants.BUTTON_CANCEL));
	    cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setRowData();
				setEditStatus(false);
			}
		});
	}
	
	@Override
	public void setEditStatus(boolean isEdit) {
		this.isEdit = isEdit;
		JComponent[] forms = {sourceField, targetField};
		loadComponentStatus(forms, isEdit);
	    loadComponentStatus(new JComponent[]{addLineBtn, delLineBtn, saveBtn, cancelBtn}, isEdit);
	    loadComponentStatus(new JComponent[]{editBtn}, !isEdit);
	    resetDataModel(isEdit);
	}
	
	private void initData() throws Exception {
		columnNames = new Vector<String>();
		columnNames.add("tablename");
		columnNames.add("source");
		columnNames.add("target");
		rowData = new Vector<Vector<String>>();
		mapList = Context.getInstance().getMapperList();
		setRowData();
	}
	
	private void setRowData() {
		if(mapList!=null&&!mapList.isEmpty()) {
			rowData.clear();
			for (int i = 0; i < mapList.size(); i++) {
				String tablename = mapList.get(i).getTablename();
				String source = mapList.get(i).getSource();
				String target = mapList.get(i).getTarget();
				Vector<String> v = new Vector<String>();
				v.add(tablename);
				v.add(source);
				v.add(target);
				rowData.add(v);
			}
		}
	}
	
	private void resetDataModel(final boolean bool) {
		dataModel = new DefaultTableModel(rowData, columnNames){
			private static final long serialVersionUID = 7469294117927470998L;
			@Override
			public boolean isCellEditable(int row, int column) {
				return bool;
			}
		};
		table.setModel(dataModel);
	}

}
