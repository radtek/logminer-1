package com.logminerplus.core;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import com.logminerplus.bean.Log;
import com.logminerplus.bean.Mapper;
import com.logminerplus.jdbc.DataBase;
import com.logminerplus.utils.Context;

public class Miner {
	
	private static Logger logger = Logger.getLogger(Miner.class.getName());
	
	private static org.apache.commons.logging.Log sqlsLog = LogFactory.getLog("sqls");
	
	private String dictionary;
	private String sourceName;
	private String targetName;
	private List<Mapper> mapperList;
	
	public Miner() {
		try {
			dictionary = Context.getInstance().getFileSet().getDictdir();
			sourceName = Context.getInstance().getDataSource("source").getName().toUpperCase();
			targetName = Context.getInstance().getDataSource("target").getName().toUpperCase();
			mapperList = Context.getInstance().getMapperList();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}
	
	synchronized public void startLogMiner(List<Log> logList) throws Exception {
		logger.info("################Start################");
		long start = System.currentTimeMillis();
		Connection sourceConn = null;
		Connection targetConn = null;
		ResultSet resultSet = null;
		try {
			sourceConn = DataBase.getSourceDataBase();
			targetConn = DataBase.getTargetDataBase();
			targetConn.setAutoCommit(false);
			Statement sourceStat = sourceConn.createStatement();
			Statement targetStat = targetConn.createStatement();
			if(logList!=null&&!logList.isEmpty()) {
				boolean isError = false;
				String filename = logList.get(0).getFilename();
				String sql = add_logfile(logList);
				//********
				CallableStatement callableStatement = sourceConn.prepareCall(sql);
				callableStatement.execute();
				resultSet = sourceStat.executeQuery("SELECT db_name, thread_sqn, filename FROM v$logmnr_logs");
				while(resultSet.next()){
					logger.info("# Added Flie:"+resultSet.getObject(3));
				}
				long startScn = getLastScn(targetStat);
				long nextScn = startScn;
				logger.info("# StartScn:"+startScn);
				callableStatement = sourceConn.prepareCall(start_logmnr(0, dictionary));
				callableStatement.execute();
				long minScn = getMinScn(sourceStat);
				long maxScn = getMaxScn(sourceStat);
				logger.info("# MinScn:"+minScn);
				logger.info("# MaxScn:"+maxScn);
				logger.info("# Result:");
				
				resultSet = sourceStat.executeQuery(getSql(startScn));
				String redo = null; 
				String last = null;
				int c = 0;
				int commit = 1000;
				int total = 0;
				while(resultSet.next()) {
					nextScn = resultSet.getLong(1);
					//redo sql
					redo = resultSet.getString(5);
					if(redo.startsWith("insert")||redo.startsWith("delete")||redo.startsWith("update")) {
						//last sql
						last = MinerConvert.convertSql(redo, sourceName, targetName, mapperList,false);
						//logger.info("# redo: "+nextScn+" "+redo);
						//logger.info("# last: "+nextScn+" "+last);
						//run  sql
						try {
							targetStat.executeUpdate(last);
//							sqlsLog.info(filename + " " + nextScn + " " + last);
						} catch (Exception e) {
//							logger.error("Error:"+nextScn, e);
							sqlsLog.error(filename + " " + nextScn + " " + last);
							isError = true;
						}
					} else {
						sqlsLog.error(filename + " " + nextScn + " " + redo);
					}
					c++;
					total++;
					if(commit==c) {
						//更新最终的SCN，在目标库提交SQL
						targetStat.executeUpdate("update scn set lastscn="+nextScn+", lasttime=current_timestamp()");
						targetConn.commit();
						//重置统计
						c=0;
						logger.info("# Count:" + total);
					}
				}
				if(c>0) {
					targetStat.executeUpdate("update scn set lastscn="+nextScn+", lasttime=current_timestamp()");
					targetConn.commit();
				}
				logger.info("# Total:" + total);
				if(!isError) {
					sqlsLog.info("#");
				} else {
					sqlsLog.error("#");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("LogMiner Error", e);
			throw new Exception("LogMiner Error", e);
		} finally {
			if(sourceConn!=null) {
				sourceConn.close();
			}
			if(targetConn!=null) {
				targetConn.close();
			}
		}
		long end = System.currentTimeMillis();
		Calendar c = Calendar.getInstance(); 
		c.setTimeInMillis(end-start);
		logger.info("# Time:" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND) + "." + c.get(Calendar.MILLISECOND) + "");
		logger.info("##################End##################");
	}
	
	/**
	 * add_logfile
	 * @param logFilePathList
	 * @return
	 */
	private String add_logfile(List<Log> log) {
		StringBuffer sb = new StringBuffer();
		sb.append("BEGIN                                                                              ");
		sb.append("  dbms_logmnr.add_logfile(logfilename => '"+log.get(0).getAbsolutePath()+"',       ");
		sb.append("                          options     => dbms_logmnr.NEW);                         ");
		for (int i = 1; i < log.size(); i++) {
			sb.append("  dbms_logmnr.add_logfile(logfilename => '"+log.get(i).getAbsolutePath()+"',   ");
			sb.append("                          options     => dbms_logmnr.ADDFILE);                 ");
		}
		sb.append("END;                                                                               ");
		return sb.toString();
	}

	/**
	 * start_logmnr
	 * @param startScn
	 * @param dictionary
	 * @return
	 */
	private String start_logmnr(long startScn, String dictionary) {
		//OPTIONS =>DBMS_LOGMNR.COMMITTED_DATA_ONLY+dbms_logmnr.NO_ROWID_IN_STMT
		String sql = "BEGIN dbms_logmnr.start_logmnr(startScn=>"+startScn+",dictfilename=>'"+dictionary+"/dictionary.ora',OPTIONS =>DBMS_LOGMNR.COMMITTED_DATA_ONLY+dbms_logmnr.NO_ROWID_IN_STMT);END;";
		return sql;
	}
	
	private String getSql(long scn) {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT scn, operation, timestamp, status, sql_redo ");
		sb.append("  FROM v$logmnr_contents                                    ");
		sb.append(" WHERE 1 = 1                                                ");
		//sb.append("   AND scn > "+scn+"                                        ");
		sb.append("   AND seg_owner = '"+sourceName+"'                         ");
		//sb.append("   AND seg_type_name in ('TABLE', 'TABPART')                ");
		//sb.append("   AND username = '"+sourceName+"'                          ");
		//sb.append("   AND operation != 'SELECT_FOR_UPDATE'                     ");
		sb.append("   AND operation != 'DDL'                                   ");
		sb.append("   AND operation != 'UNSUPPORTED'                           ");
		sb.append(" ORDER BY scn                                               ");
		return sb.toString();
	}
	
	/**
	 * 字典文件更新
	 * @param sourceConn
	 * @throws Exception
	 */
	public void createDictionary(Connection sourceConn) throws Exception {
		String createDictSql = "BEGIN dbms_logmnr_d.build(dictionary_filename => 'dictionary.ora', dictionary_location =>'"+dictionary+"'); END;";
		CallableStatement callableStatement = sourceConn.prepareCall(createDictSql);
		callableStatement.execute();
		logger.info("Dictionary Update Successful!");
	}
	
	private long getMinScn(Statement stat) throws Exception {
		long scn = 0;
		String sql = "select min(scn) FROM v$logmnr_contents";
		ResultSet rs = stat.executeQuery(sql);
		while(rs.next()) {
			scn = rs.getLong(1);
		}
		return scn;
	}
	
	private long getMaxScn(Statement stat) throws Exception {
		long scn = 0;
		String sql = "select max(scn) FROM v$logmnr_contents";
		ResultSet rs = stat.executeQuery(sql);
		while(rs.next()) {
			scn = rs.getLong(1);
		}
		return scn;
	}
	
	/**
	 * 获取上次同步的SCN号-中间表
	 * @param stat
	 * @return
	 * @throws Exception
	 */
	private long getLastScn(Statement stat) throws Exception {
		long scn = 0;
		String sql = "select * from scn limit 0, 1";
		ResultSet rs = stat.executeQuery(sql);
		while(rs.next()) {
			scn = rs.getLong(1);
		}
		return scn;
	}
	
}
