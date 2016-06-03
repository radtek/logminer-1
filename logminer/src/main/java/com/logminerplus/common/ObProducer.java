package com.logminerplus.common;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.logminerplus.bean.Log;
import com.logminerplus.bean.Mapper;
import com.logminerplus.core.MinerConvert;
import com.logminerplus.jdbc.DataBase;
import com.logminerplus.utils.Context;

class dmlStore {
    public String redo = null;
    public String rowId = null;
    public ObPacketType type = ObPacketType.ERROR;
    public long scn = ObDefine.OB_INVALID_ID;
    public int updateRowCount = 0;
    public int[] conflictPos = new int[2];

    public dmlStore() {
        reset();
    }

    public void reset() {
        redo = null;
        rowId = null;
        type = ObPacketType.ERROR;
        scn = ObDefine.OB_INVALID_ID;
        conflictPos[0] = ObDefine.OB_INVALID_ID;
        conflictPos[1] = ObDefine.OB_INVALID_ID;
    }

    public void copy(dmlStore other) {
        this.redo = other.redo;
        this.rowId = other.rowId;
        this.type = other.type;
        this.scn = other.scn;
        this.updateRowCount = other.updateRowCount;
        this.conflictPos = Arrays.copyOf(other.conflictPos, 2);
    }

    public void incRowCount() {
        updateRowCount++;
    }

    public boolean ifBatchUpdateSafe() {
        return updateRowCount < ObDefine.OB_SAFE_BATCH_UPDATE_NUM;
    }

    public int[] getConflictRowThreadId(String rowId) {
        return ObDefine.COMMIT_QUEUE.getConflictThreadId(rowId);
    }

    public void handleConlictRow(String rowId) {
        int[] pos = new int[2];
        pos = getConflictRowThreadId(rowId);
        if (pos[0] != ObDefine.OB_INVALID_ID) {
            if (ObDefine.OB_INVALID_ID == conflictPos[0]) {
                conflictPos = Arrays.copyOf(pos, 2);
            } else if (pos[0] < conflictPos[0]) {
                conflictPos = Arrays.copyOf(pos, 2);
            }
        }
    }
}

class dmlStorePool {
    private dmlStore head = new dmlStore();
    private dmlStore tail = new dmlStore();
    public boolean empty;

    public dmlStorePool() {
        tail.reset();
        head.reset();
    }

    public int addIn(dmlStore e) {

        int ret = ObDefine.OB_SUCCESS;
        if (ObDefine.OB_INVALID_ID == head.scn && ObDefine.OB_INVALID_ID == tail.scn) {
            head.copy(e);
            head.handleConlictRow(e.rowId);
            ret = ObDefine.OB_NEED_AGAIN;
        } else if (ObDefine.OB_INVALID_ID == tail.scn) {
            if (0 == head.redo.compareTo(e.redo)) {
                ret = ObDefine.OB_NEED_AGAIN;
                head.type = ObPacketType.BATCHUPDATE;
                head.incRowCount();
                head.handleConlictRow(e.rowId);
            } else {
                tail.copy(e);
                tail.handleConlictRow(e.rowId);
            }
        } else {
            ret = ObDefine.OB_NEED_STOP;
        }
        return ret;
    }

    public void pop() {
        head.copy(tail);
        tail.reset();
    }

    public dmlStore getOne() {
        dmlStore ret = new dmlStore();
        ret.copy(head);
        return ret;
    }

    public boolean isEmpty() {
        return (ObDefine.OB_INVALID_ID == head.scn && ObDefine.OB_INVALID_ID == tail.scn);
    }

}

public class ObProducer {
    private static String dictionary;
    private static String sourceName;
    private static String targetName;
    private static List<Mapper> mapperList;

    private static Connection sourceConn = null;
    private static Connection targetConn = null;
    private static ResultSet resultSet = null;
    private long minScn;
    private long maxScn;
    private long curScn;
    private static String lastRowId;
    private static String lastRedo;

    private static boolean hasBatchUpdate;

    private static String batchUpdateRowId;
    private static String batchUpdateRedo;
    private long batchUpdateScn;

    private dmlStorePool pool = new dmlStorePool();
    public int seq = 0;
    private boolean cond = false;

    public ObProducer() {
    }

    public static void init() {
        ObDefine.COMMIT_QUEUE.init();
        lastRowId = null;
        lastRedo = null;
        batchUpdateRowId = null;
        batchUpdateRedo = null;
        hasBatchUpdate = false;
        ObDefine.hash.put("HS_KPI_EWB", 1);
        ObDefine.hash.put("HS_GLOBAL_BUSI_LOG", 1);
        ObDefine.hash.put("SEND_OPERATION_ID", 1);
        ObDefine.hash.put("HS_AUTH_USER_ROLE", 1);
        ObDefine.hash.put("TEMP_FIN_SUM_BALANCE", 1);
        ObDefine.hash.put("HS_AUTH_LOGIN_HISTORY", 1);
        ObDefine.hash.put("HS_EWBS_LIST_DETAIL", 1);
        ObDefine.hash.put("HS_QUOTE_EX_PROP", 1);

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
        long start = System.currentTimeMillis();
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

    public String getLastRedo() {
        return lastRedo;
    }

    public String getLastRowId() {
        if (hasBatchUpdate) {
            return batchUpdateRowId;
        } else {
            return lastRowId;
        }
    }

    public void batchUpdateReset() {
        hasBatchUpdate = false;
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

    public void startLogMnr(List<Log> logList) throws Exception {
        try {
            startConn();
            Statement sourceStat = sourceConn.createStatement();
            Statement targetStat = targetConn.createStatement();
            if (logList != null && !logList.isEmpty()) {
                boolean isError = false;
                String filename = logList.get(0).getFilename();
                String sql = add_logfile(logList);

                // ********
                CallableStatement callableStatement = sourceConn.prepareCall(sql);
                callableStatement.execute();
                resultSet = sourceStat.executeQuery("SELECT db_name, thread_sqn, filename FROM v$logmnr_logs");
                while (resultSet.next()) {
                    ObDefine.logger.info("# Added Flie:" + resultSet.getObject(3));
                }
                long startScn = getLastScn(targetStat);
                long nextScn = startScn;
                ObDefine.logger.info("# StartScn:" + startScn);
                callableStatement = sourceConn.prepareCall(start_logmnr(0, dictionary));
                callableStatement.execute();
                ObDefine.logger.info("# Result:");
                resultSet = sourceStat.executeQuery(getSql(startScn));

            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public int next(ObDMLPacket packet) throws Exception {
        int ret = ObDefine.OB_ERROR;
        dmlStore dml = new dmlStore();
        String tmp, convert, tableName = null;
        while (true) {
            if (resultSet.next()) {
                tmp = resultSet.getString(6);
                tableName = resultSet.getString(7);
                if ((0 == ObDefine.OB_HANDLE_UPDATE) && tmp.startsWith("update"))
                    continue;
                if (!(tmp.startsWith("insert") || tmp.startsWith("delete") || tmp.startsWith("replace") || tmp.startsWith("update"))
                        || tableName.startsWith("HS_GLOBAL_TASK"))
                    continue;
                if (tableName.startsWith("JBPM4_HIST") || tableName.startsWith("JBPM4_TASK"))
                    continue;
                if (ObDefine.hash.containsKey(tableName))
                    continue;
                convert = MinerConvert.convertSql(tmp, sourceName, targetName, mapperList, true);
                dml.redo = convert;
                dml.scn = resultSet.getLong(2);
                dml.rowId = resultSet.getString(1);
                seq++;
                if (ObDefine.OB_NEED_AGAIN != (ret = pool.addIn(dml))) {
                    break;
                }
                packet.addTableName(dml.rowId);
                if (dml.type == ObPacketType.BATCHUPDATE)
                    ObDefine.logger.info("break" + dml.redo);
            } else {
                ret = ObDefine.OB_ITER_END;
            }

        }
        return ret;
    }

    public ObDMLPacket getNextPacket() throws Exception {
        ObDMLPacket packet = new ObDMLPacket();
        int tmpRet = ObDefine.OB_SUCCESS;
        packet.reset();
        dmlStore dml = new dmlStore();
        int tid = ObDefine.OB_INVALID_ID;
        packet.setPacketType(ObPacketType.TRANSACTION); // default situation
        while (true) {
            if (ObDefine.OB_SAFE_DML_NUM <= packet.getArraySize()) {
                break;
            }
            if (ObDefine.OB_ITER_END == (tmpRet = next(packet)) && pool.isEmpty()) {
                packet.setPacketType(ObPacketType.LASTPACKET);
                break;
            } else {
                dml = pool.getOne();
                if (ObPacketType.BATCHUPDATE == dml.type) {
                    if ((!dml.ifBatchUpdateSafe()) && packet.isEmpty()) {
                        packet.setPacketType(dml.type);
                        packet.addDML(dml.redo, dml.rowId, dml.updateRowCount);
                        packet.setCommitScn(dml.scn);
                        if (ObDefine.OB_INVALID_ID != dml.conflictPos[0]) {
                            packet.conflictPos_ = dml.conflictPos[0];
                            packet.setThreadID(dml.conflictPos[1]);
                        }
                        packet.setSeq(seq);
                        pool.pop();
                        break;
                    } else if ((!dml.ifBatchUpdateSafe()) && !packet.isEmpty()) {
                        break;
                    } else if (dml.ifBatchUpdateSafe()) {
                        packet.setPacketType(dml.type);
                        packet.addDML(dml.redo, dml.rowId, dml.updateRowCount);
                        packet.setCommitScn(dml.scn);
                        if (ObDefine.OB_INVALID_ID != dml.conflictPos[0]) {
                            if (ObDefine.OB_INVALID_ID != packet.conflictPos_ && (packet.conflictPos_ > dml.conflictPos[0])) {
                                packet.conflictPos_ = dml.conflictPos[0];
                                packet.setThreadID(dml.conflictPos[1]);
                            } else if (ObDefine.OB_INVALID_ID == packet.conflictPos_) {
                                packet.conflictPos_ = dml.conflictPos[0];
                                packet.setThreadID(dml.conflictPos[1]);
                            }
                        }
                        packet.setSeq(seq);
                        pool.pop();
                    }
                } else {
                    if (ObDefine.OB_INVALID_ID != (dml.conflictPos[0])) {
                        if (ObDefine.OB_INVALID_ID == packet.conflictPos_) {
                            packet.conflictPos_ = dml.conflictPos[0];
                            packet.setThreadID(dml.conflictPos[1]);
                            packet.addDML(dml.redo, dml.rowId, dml.updateRowCount);
                            packet.setCommitScn(dml.scn);
                            packet.setSeq(seq);
                            pool.pop();
                        } else if (packet.conflictPos_ > dml.conflictPos[0]) {
                            packet.conflictPos_ = dml.conflictPos[0];
                            packet.setThreadID(dml.conflictPos[1]);
                            packet.addDML(dml.redo, dml.rowId, dml.updateRowCount);
                            packet.setCommitScn(dml.scn);
                            pool.pop();
                            packet.setSeq(seq);
                        } else {
                            packet.addDML(dml.redo, dml.rowId, dml.updateRowCount);
                            packet.setCommitScn(dml.scn);
                            pool.pop();
                            packet.setSeq(seq);
                        }
                    } else {
                        packet.addDML(dml.redo, dml.rowId, dml.updateRowCount);
                        packet.setCommitScn(dml.scn);
                        packet.setSeq(seq);
                        pool.pop();
                    }
                }
            }
        }
        if (ObDefine.OB_SAFE_DML_NUM > packet.getArraySize()) {
            ObDefine.logger.info("size = " + packet.getArraySize() + "type = " + packet.getType());
        }
        return packet;
    }

    public void updateBloomFilter(ObDMLPacket dml, int tid) {
        Iterator<String> itr = dml.gettableNameIter();
        String rid = null;
        while (itr.hasNext()) {
            rid = itr.next();
        }
    }

    public int checkBloomFilter(String RowId) {
        int ret = ObDefine.OB_INVALID_ID;
        return ret;
    }

    public int nextBak() throws Exception {
        int ret = ObDefine.OB_ERROR;
        String tmp = null;
        String convert = null;
        while (resultSet.next()) {
            tmp = resultSet.getString(6);

            if (!(tmp.startsWith("insert") || tmp.startsWith("update") || tmp.startsWith("delete") || tmp.startsWith("replace")))
                continue;
            convert = MinerConvert.convertSql(tmp, sourceName, targetName, mapperList, false);
            if (lastRedo != null) {
                if (0 != convert.compareTo(lastRedo)) {
                    if (ret != ObDefine.OB_BATCH_UPDATE) {
                        ret = ObDefine.OB_SUCCESS;
                    }
                    lastRowId = resultSet.getString(1);
                    curScn = resultSet.getLong(2);
                    lastRedo = convert;
                    seq++;
                    break;
                } else if (!hasBatchUpdate) {
                    ret = ObDefine.OB_BATCH_UPDATE;
                    hasBatchUpdate = true;
                    batchUpdateRowId = resultSet.getString(1);
                    batchUpdateRedo = convert;
                    batchUpdateScn = resultSet.getLong(2);
                }
            } else {
                lastRowId = resultSet.getString(1);
                curScn = resultSet.getLong(2);
                lastRedo = convert;
                ret = ObDefine.OB_SUCCESS;
                seq++;
                break;
            }
        }

        if (ObDefine.OB_ERROR == ret) {
            if (maxScn != curScn) // failed to get last row
            {
                throw new ObException("failed to exec next func!");
            } else {
                ret = ObDefine.OB_ITER_END;
            }

        }
        return ret;
    }

    public String get_next_sql() throws SQLException {
        if (hasBatchUpdate) {
            return batchUpdateRedo;
        } else {
            return lastRedo;
        }

    }

    public long get_next_scn() {
        if (hasBatchUpdate) {
            return batchUpdateScn;
        } else {
            return curScn;
        }
    }

    private long getMinScn(Statement stat) throws Exception {
        long scn = 0;
        String sql = "select min(scn) FROM v$logmnr_contents";
        ResultSet rs = stat.executeQuery(sql);
        while (rs.next()) {
            scn = rs.getLong(1);
        }
        return scn;
    }

    private long getMaxScn(Statement stat) throws Exception {
        long scn = 0;
        String sql = "select max(scn) FROM v$logmnr_contents";
        ResultSet rs = stat.executeQuery(sql);
        while (rs.next()) {
            scn = rs.getLong(1);
        }
        return scn;
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
        sb.append("SELECT row_id, scn, operation, timestamp, status, sql_redo,table_name ");
        sb.append("  FROM v$logmnr_contents                                    ");
        sb.append(" WHERE 1 = 1                                                ");
        // sb.append("   AND scn > "+scn+"                                        ");
        sb.append("   AND seg_owner = '" + sourceName + "'                         ");
        // sb.append("   AND seg_type_name in ('TABLE', 'TABPART')                ");
        // sb.append("   AND username = '"+sourceName+"'                          ");
        // sb.append("   AND operation != 'SELECT_FOR_UPDATE'                     ");
        sb.append("   AND operation != 'DDL'                                   ");
        sb.append("   AND operation != 'UNSUPPORTED'                           ");
        sb.append(" ORDER BY scn                                               ");
        return sb.toString();
    }

}
