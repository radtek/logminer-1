package com.logminerplus.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;

import com.logminerplus.bean.DataSource;
import com.logminerplus.utils.Context;

public class DataBase {
    public static synchronized Connection getConnection(String ele) throws Exception {
        Connection conn = null;
        DataSource ds = Context.getInstance().getDataSource(ele);
        String type = ds.getType();
        if ("Oracle".equals(type)) {
            String url = "jdbc:oracle:thin:@" + ds.getAddress() + ":" + ds.getPort() + ":" + ds.getSid();
            Class.forName(ds.getDriver());
            conn = DriverManager.getConnection(url, ds.getUsername(), ds.getPassword());
        } else if ("OBase".equals(type)) {
            Class.forName(ds.getDriver());
            String url = "jdbc:mysql://" + ds.getAddress() + ":" + ds.getPort();
            conn = DriverManager.getConnection(url, ds.getUsername(), ds.getPassword());
        } else {
            throw new Exception("不支持的数据库类型！");
        }
        return conn;
    }

    public static Connection getSourceDataBase() throws Exception {
        return getConnection("source");
    }

    public static Connection getTargetDataBase() throws Exception {
        return getConnection("target");
    }

}
