package com.logminerplus.common;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import com.logminerplus.bean.Log;
import com.logminerplus.bean.Mapper;
import com.logminerplus.common.ObDefine;
import com.logminerplus.jdbc.DataBase;
import com.logminerplus.utils.Context;
import com.logminerplus.common.ObDMLPacket;
import com.logminerplus.core.MinerConvert;

class newDmlRecord {

    public String xid = null;
    public String dml = null;
    public String rsID = null;
    public int operation = ObDefine.OB_INVALID_ID;
    public int ssn = ObDefine.OB_INVALID_ID;
    public int csf = ObDefine.OB_INVALID_ID;
    public int rollback = ObDefine.OB_INVALID_ID;
    public String tableName = null;
    public String owner = null;
    public ObPacketType type = ObPacketType.ERROR;
    public int updateRowCount = 0;
    public int[] conflictThread = new int[2]; // 0 for thread index, 1 for
                                              // thread no
    public String segType = null;
    public String block = null;
    public int status = ObDefine.OB_INVALID_ID;

    public newDmlRecord() {
        reset();
    }

    public void reset() {
        xid = null;
        dml = null;
        rsID = null;
        operation = ObDefine.OB_INVALID_ID;
        ssn = ObDefine.OB_INVALID_ID;
        csf = ObDefine.OB_INVALID_ID;
        rollback = ObDefine.OB_INVALID_ID;
        tableName = null;
        owner = null;
        type = ObPacketType.ERROR;
        conflictThread[0] = ObDefine.OB_INVALID_ID;
        conflictThread[1] = ObDefine.OB_INVALID_ID;
        updateRowCount = 0;
        segType = null;
        block = null;
        status = ObDefine.OB_INVALID_ID;
    }

    public void copy(newDmlRecord other) {
        this.xid = other.xid;
        this.dml = other.dml;
        this.rsID = other.rsID;
        this.operation = other.operation;
        this.ssn = other.ssn;
        this.csf = other.csf;
        this.rollback = other.rollback;
        this.tableName = other.tableName;
        this.type = other.type;
        this.updateRowCount = other.updateRowCount;
        this.owner = other.owner;
        this.conflictThread = Arrays.copyOf(other.conflictThread, 2);
        this.segType = other.segType;
        this.block = other.block;
        this.status = other.status;
    }

    public void incUpdateRowCount() {
        this.updateRowCount++;
    }

    public boolean ifBatchUpdateSafe() {
        return updateRowCount < ObDefine.OB_SAFE_BATCH_UPDATE_NUM;
    }

    public int[] getConflictRowThread(String tableName) {
        return ObDefine.COMMIT_QUEUE.getConflictThreadId(tableName);
    }

    public void handleConlictRow(String blockstr) {
        int[] pos = new int[2];
        pos = Arrays.copyOf(getConflictRowThread(blockstr), 2);
        if (ObDefine.OB_INVALID_ID != pos[0]) {
            if (ObDefine.OB_INVALID_ID == conflictThread[0]) {
                conflictThread = Arrays.copyOf(pos, 2);
            } else if (pos[0] < conflictThread[0]) {
                conflictThread = Arrays.copyOf(pos, 2);
            }
        }
    }
}

class newDmlPool {
    private newDmlRecord head = new newDmlRecord();
    private newDmlRecord tail = new newDmlRecord();

    public newDmlPool() {
        head.reset();
        tail.reset();
    }

    public int addIn(newDmlRecord e) {
        int ret = ObDefine.OB_SUCCESS;
        if (ObDefine.OB_INVALID_ID == head.ssn && ObDefine.OB_INVALID_ID == tail.ssn) {
            head.copy(e);
            if (null != e.block)
                head.handleConlictRow(e.block);
            ret = ObDefine.OB_NEED_AGAIN;
        } else if (ObDefine.OB_INVALID_ID == tail.ssn) {
            if (0 == head.dml.compareTo(e.dml)) {
                ret = ObDefine.OB_NEED_AGAIN;
                head.type = ObPacketType.BATCHUPDATE;
                head.incUpdateRowCount();
                if (null != e.block)
                    head.handleConlictRow(e.block);
            } else {
                tail.copy(e);
                if (null != e.block)
                    tail.handleConlictRow(e.block);
            }
        } else {
            ret = ObDefine.OB_NEED_STOP;
        }
        return ret;
    }

    public void pop() {
        head.copy(tail);
        tail.reset();
        ;
        // return ret;
    }

    public newDmlRecord getOne() {
        newDmlRecord ret = new newDmlRecord();
        ret.copy(head);
        return ret;
    }

    public boolean isEmpty() {
        return (ObDefine.OB_INVALID_ID == head.ssn && ObDefine.OB_INVALID_ID == tail.ssn);
    }
}

public class ObNewProducer {
    private static String dictionary;
    private static String sourceName;
    private static String targetName;
    private static List<Mapper> mapperList;
    private static Connection sourceConn = null;
    private static Connection targetConn = null;
    private static ResultSet resultSet = null;
    private newDmlPool pool = new newDmlPool();
    private ObDMLPacket curPacket = new ObDMLPacket();
    public int seq = 0;

    // public int batchUpdateCount = 0;

    public ObNewProducer() {
    }

    public void init() {
        ObDefine.COMMIT_QUEUE.init();
        ObDefine.tablenameSet.add("HS_KPI_EWB");
        ObDefine.tablenameSet.add("HS_GLOBAL_BUSI_LOG");
        ObDefine.tablenameSet.add("SEND_OPERATION_ID");
        ObDefine.tablenameSet.add("HS_AUTH_USER_ROLE");
        ObDefine.tablenameSet.add("TEMP_FIN_SUM_BALANCE");
        ObDefine.tablenameSet.add("HS_AUTH_LOGIN_HISTORY");
        ObDefine.tablenameSet.add("HS_EWBS_LIST_DETAIL");
        ObDefine.tablenameSet.add("HS_QUOTE_EX_PROP");
        // ObDefine.hash.put();
        // for(int i = 0; i < ObDefine.OB_THREAD_NUM; i++)
        // {
        // bloomFilter[i] = new
        // BloomFilter<String>(ObDefine.OB_BLOOM_FILTER_POSITIVE,
        // ObDefine.OB_EXPECT_BM_SIZE);
        // }

        try {
            dictionary = Context.getInstance().getFileSet().getDictdir();
            sourceName = Context.getInstance().getDataSource("source").getName().toUpperCase();
            targetName = Context.getInstance().getDataSource("target").getName().toUpperCase();
            mapperList = Context.getInstance().getMapperList();
        } catch (Exception e) {
            e.printStackTrace();
            ObDefine.logger.error(e);
        }
    }

    public static void startConn() {
        ObDefine.logger.info("################Start Conn################");
        // long start = System.currentTimeMillis();
        sourceConn = null;
        targetConn = null;
        resultSet = null;
        try {
            sourceConn = DataBase.getSourceDataBase();
            targetConn = DataBase.getTargetDataBase();
            targetConn.setAutoCommit(false);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * add_logfile
     * 
     * @param logFilePathList
     * @return
     */
    private String add_logfile(List<Log> log) {
        StringBuffer sb = new StringBuffer();
        sb.append("BEGIN                                                                              ");
        sb.append("  dbms_logmnr.add_logfile(logfilename => '" + log.get(0).getAbsolutePath() + "',       ");
        sb.append("                          options     => dbms_logmnr.NEW);                         ");
        for (int i = 1; i < log.size(); i++) {
            sb.append("  dbms_logmnr.add_logfile(logfilename => '" + log.get(i).getAbsolutePath() + "',   ");
            sb.append("                          options     => dbms_logmnr.ADDFILE);                 ");
        }
        sb.append("END;                                                                               ");
        return sb.toString();
    }

    /**
     * 获取上次同步的SCN号-中间表
     * 
     * @param stat
     * @return
     * @throws Exception
     */
    private long getLastScn(Statement stat) throws Exception {
        long scn = 0;
        String sql = "select * from scn limit 0, 1";
        ResultSet rs = stat.executeQuery(sql);
        while (rs.next()) {
            scn = rs.getLong(1);
        }
        return scn;
    }

    public void startLogMnr(List<Log> logList) throws Exception {
        try {
            startConn();
            Statement sourceStat = sourceConn.createStatement();
            Statement targetStat = targetConn.createStatement();
            if (logList != null && !logList.isEmpty()) {
                // 1.添加日志文件
                String sql = add_logfile(logList);
                CallableStatement callableStatement = sourceConn.prepareCall(sql);
                callableStatement.execute();
                
                // 2.查看日志文件是否添加成功
                ResultSet logmnr_logs_rs = sourceStat.executeQuery("SELECT db_name, thread_sqn, filename FROM v$logmnr_logs");
                while (logmnr_logs_rs.next()) {
                    ObDefine.logger.info("# Added Flie:" + logmnr_logs_rs.getObject(3));
                }
                
                // 3.从同步目标库中获取最大的scn号
                long startScn = getLastScn(targetStat);
                ObDefine.logger.info("# StartScn:" + startScn);
                
                // 4.添加字典文件
                callableStatement = sourceConn.prepareCall(start_logmnr(0, dictionary));
                ObDefine.logger.debug("#dictionary is => " + dictionary);
                ObDefine.logger.debug("#callableStatement is => " + start_logmnr(0, dictionary));
                callableStatement.execute();
                ObDefine.logger.info("# Result:");
                
                // 5.从v$logmnr_contents表中查询对应的内容
                resultSet = sourceStat.executeQuery(getSql(startScn));
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * start_logmnr
     * 
     * @param startScn
     * @param dictionary
     * @return
     */
    private String start_logmnr(long startScn, String dictionary) {
        // OPTIONS
        // =>DBMS_LOGMNR.COMMITTED_DATA_ONLY+dbms_logmnr.NO_ROWID_IN_STMT
        String sql = "BEGIN dbms_logmnr.start_logmnr(startScn=>" + startScn + ",dictfilename=>'" + dictionary
                + "/dictionary.ora',OPTIONS =>DBMS_LOGMNR.COMMITTED_DATA_ONLY+dbms_logmnr.NO_ROWID_IN_STMT);END;";
        return sql;
    }

    private String getSql(long scn) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT xid, operation_code, table_name, sql_redo, rs_id, ssn, csf, rollback, seg_owner,seg_type_name, ABS_FILE#,REL_FILE#,DATA_BLK#,DATA_OBJ#,row_id");
        sb.append("  FROM v$logmnr_contents                                    ");
        sb.append(" WHERE 1 = 1                                                ");
        // sb.append("   AND scn > "+scn+"                                        ");
        // sb.append("   AND seg_owner = '"+sourceName+"'                         ");
        // sb.append("   AND seg_type_name in ('TABLE', 'TABPART')                ");
        // sb.append("   AND username = '"+sourceName+"'                          ");
        // sb.append("   AND operation != 'SELECT_FOR_UPDATE'                     ");
        // sb.append("   AND operation != 'DDL'                                   ");
        // sb.append("   AND operation != 'UNSUPPORTED'                           ");
        // sb.append(" ORDER BY scn                                               ");
        return sb.toString();
    }

    public int next(ObDMLPacket curPacket) throws Exception {
        int ret = ObDefine.OB_SUCCESS;
        newDmlRecord record = new newDmlRecord();
        String tmp, convert, tableName, segType = null;
        StringBuilder buf = new StringBuilder();
        int ABS_FILE, REL_FILE, DATA_BLK, DATA_OBJ = 0;
        while (true) {
            if (resultSet.next()) {
                tmp = resultSet.getString(4);
                tableName = resultSet.getString(3);
                segType = resultSet.getString(10);
                record.operation = resultSet.getInt(2);
                if ((0 == ObDefine.OB_HANDLE_UPDATE) && record.operation == ObDefine.OB_UPDATE)
                    continue;
                if (null != tableName
                        && (tableName.startsWith("JBPM4_HIST") || tableName.startsWith("JBPM4_TASK") || tableName.startsWith("HS_GLOBAL_TASK")))
                    continue;
                if (null != tableName && ObDefine.tablenameSet.contains(tableName))
                    continue;
                convert = MinerConvert.convertSql(tmp, sourceName, targetName, mapperList, true);
                record.xid = resultSet.getString(1);

                record.tableName = tableName;
                record.dml = convert;
                record.rsID = resultSet.getString(5);
                record.ssn = resultSet.getInt(6);
                record.csf = resultSet.getInt(7);
                record.rollback = resultSet.getInt(8);
                record.owner = resultSet.getString(9);
                record.segType = segType;
                ABS_FILE = resultSet.getInt(11);
                REL_FILE = resultSet.getInt(12);
                DATA_BLK = resultSet.getInt(13);
                DATA_OBJ = resultSet.getInt(14);
                if (record.operation != ObDefine.OB_COMMIT && record.operation != ObDefine.OB_START) {
                    buf.delete(0, buf.length());
                    buf.append(ABS_FILE);
                    buf.append("$");
                    buf.append(REL_FILE);
                    buf.append("$");
                    buf.append(DATA_BLK);
                    buf.append("$");
                    buf.append(DATA_OBJ);
                    record.block = buf.toString();
                } else {
                    record.block = null;
                }
                seq++;
                if (ObDefine.OB_NEED_AGAIN != (ret = pool.addIn(record))) {
                    // logger.info("break"+dml.redo);
                    if (null != record.block)
                        curPacket.addTableName(record.block);
                    break;
                }
                if (null != record.block)
                    curPacket.addTableName(record.block);
                // record.
            } else {
                ret = ObDefine.OB_ITER_END;
                break;
            }
        }
        return ret;
    }

    public int nextPacket() throws Exception {
        int ret = ObDefine.OB_SUCCESS;
        StringBuilder buf = new StringBuilder();
        curPacket.reset();
        // ObDefine.logger.info("size = "+ curPacket.getTableNameArraySize());
        newDmlRecord record = new newDmlRecord();
        while (true) {
            if (ObDefine.OB_ITER_END == (ret = next(curPacket)) && pool.isEmpty()) {
                curPacket.setPacketType(ObPacketType.LASTPACKET);
                break;
            } else {
                // ObDefine.logger.info("test::whx=> " +record.dml+"  "+
                // record.segType);
                record = pool.getOne();
                if (ObDefine.OB_START == record.operation) {
                    curPacket.open();
                    pool.pop();
                } else if (ObDefine.OB_COMMIT == record.operation) {
                    curPacket.close();
                    pool.pop();
                    curPacket.setSeq(seq);

                    break;
                } else if (ObDefine.OB_UNSUPPORTED == record.operation
                        || (record.segType != null && (0 != record.segType.compareTo("TABLE") && 0 != record.segType.compareTo("TABPART")))
                        || (record.tableName.startsWith("TEMP"))) {
                    pool.pop();
                    continue;
                } else {
                    if (0 != ObDefine.OB_ANE_USER.compareTo(record.owner)) {
                        curPacket.setPacketType(ObPacketType.SYSPACKET);
                    } else {

                        buf.append(record.dml);
                        // ObDefine.logger.info("add DML = > "+ buf.toString() +
                        // "csf=> "+record.csf);
                        if (0 == record.csf) {
                            // buf.append(record.dml);
                            curPacket.addDML(buf.toString(), record.tableName, record.updateRowCount);
                            buf.delete(0, buf.length());
                        }
                        if (ObPacketType.BATCHUPDATE == record.type) {
                            curPacket.batchUpdateCount += record.updateRowCount;
                        }
                        if (ObDefine.OB_INVALID_ID != record.conflictThread[0]) {
                            if (ObDefine.OB_INVALID_ID == curPacket.conflictPos_ || curPacket.conflictPos_ > record.conflictThread[0]) {
                                curPacket.conflictPos_ = record.conflictThread[0];
                                curPacket.setThreadID(record.conflictThread[1]);
                            }
                        }
                    }
                    pool.pop();
                }

            }
        }
        // ObDefine.logger.info("produce a packet=> "+ curPacket.getArraySize()
        // + " "+ curPacket.getType());
        return ret;
    }

    public void getNextPacket(ObDMLPacket packet) throws Exception {
        // ObDMLPacket packet = new ObDMLPacket();
        int tmpRet = ObDefine.OB_SUCCESS;
        if (ObDefine.OB_PACKET_INIT != curPacket.getStatus() && ObPacketType.SYSPACKET != curPacket.getType()) {
            packet.copyPacket(curPacket);
            curPacket.reset();
        }
        // ObDefine.logger.info("in a loop");
        while (true) {
            if (ObDefine.OB_SAFE_DML_NUM <= packet.getArraySize()) {
                // logger.info("break ");
                break;
            }
            if (ObDefine.OB_ITER_END == (tmpRet = nextPacket()) && pool.isEmpty()) {
                packet.setPacketType(ObPacketType.LASTPACKET);
                break;
            } else {
                if (ObPacketType.SYSPACKET == curPacket.getType()) {
                    continue;
                } else if (ObDefine.OB_SAFE_DML_NUM <= packet.getArraySize() + curPacket.getArraySize()
                        || ObDefine.OB_SAFE_BATCH_UPDATE_NUM <= packet.batchUpdateCount + curPacket.batchUpdateCount) {
                    break;
                } else {
                    packet.copyPacket(curPacket);
                    // ObDefine.logger.info("get a packet =>" +
                    // curPacket.getSeq() + " " + curPacket.getType()
                    // +" size = "+ (packet.getArraySize() +
                    // curPacket.getArraySize()) );
                }
            }
        }
        // ObDefine.logger.info("get a loop");
        // return packet;
    }

}
