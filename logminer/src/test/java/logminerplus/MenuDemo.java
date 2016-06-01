package logminerplus;

import java.awt.*; 

public class MenuDemo {
 
    Frame f;
    MenuBar mb; 
    Menu menuFile; 
    MenuItem mItemNew, mItemOpen, mItemSave; 
 
    MenuDemo() { 
        f = new Frame("我的菜单实例"); 
        f.setBounds(300, 100, 400, 300); 
 
        mb = new MenuBar(); // 创建菜单栏MenuBar 
        menuFile = new Menu("文件");  
 
        mItemNew = new MenuItem("新建"); 
        mItemOpen = new MenuItem("打开"); 
         
        menuFile.add(mItemNew); 
        mItemSave = new MenuItem("保存"); 
         
        menuFile.add(mItemOpen); 
        // 加入分割线 
        menuFile.addSeparator(); 
        menuFile.add(mItemSave); 
        mb.add(menuFile);  
        // 菜单栏中加入“文件”菜单 
  
                // setMenuBar:将此窗体的菜单栏设置为指定的菜单栏。 
        f.setMenuBar(mb);  
        f.setVisible(true); 
    }
    
    public static void main(String[] args) { 
        new MenuDemo(); 
    } 
}
