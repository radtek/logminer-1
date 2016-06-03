package com.logminerplus.gui.pane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Timer;

public class HomePane {

    private JButton img;

    private int i = 1;

    public JPanel init() throws Exception {
        JPanel panel = new JPanel(new BorderLayout());
        img = new JButton();
        img.setSize(800, 600);
        // 按钮设置为透明
        img.setContentAreaFilled(false);
        // 内边距
        img.setMargin(new Insets(0, 0, 0, 0));
        // 凸起来的按钮
        // img.setBorder(BorderFactory.createRaisedBevelBorder());
        // 凹起来的按钮
        // img.setBorder(BorderFactory.createLoweredBevelBorder());
        // 边框绘制-无（绘制时）
        img.setBorderPainted(false);
        // 焦点绘制-无（点击时）
        img.setFocusable(false);
        final ImageIcon[] icons = new ImageIcon[2];
        icons[0] = new ImageIcon(this.getClass().getClassLoader().getResource("images/home0.png"));
        icons[1] = new ImageIcon(this.getClass().getClassLoader().getResource("images/home1.png"));
        img.setIcon(icons[0]);
        if (icons.length > 1) {
            new Timer(10000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    img.setIcon(icons[i]);
                    i++;
                    if (i == icons.length) {
                        i = 0;
                    }
                }
            }).start();
        }
        img.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                img.setIcon(icons[i]);
                i++;
                if (i == icons.length) {
                    i = 0;
                }
            }
        });
        panel.add(img);
        panel.setBackground(Color.WHITE);
        return panel;
    }

}
