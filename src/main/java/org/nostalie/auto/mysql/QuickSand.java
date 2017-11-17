package org.nostalie.auto.mysql;

import org.nostalie.auto.modify.BlackJadeKylin;
import org.nostalie.auto.mysql.datasource.DataSourceManagement;
import org.nostalie.auto.pojo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * 对增删改查方法的封装
 * @author nostalie on 17-8-18.
 */
public class QuickSand extends JdbcDaoSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuickSand.class);

    private static final String DESC_TABLE_SQL = "select TABLE_SCHEMA,TABLE_NAME,COLUMN_NAME,COLUMN_DEFAULT,IS_NULLABLE,DATA_TYPE,COLUMN_KEY,COLUMN_COMMENT " +
            "from information_schema.columns where table_name=? and table_schema=?";

    private static final int SUCCESS = 0;
    private static final int FAIL = -1;
    private static final QuickSand ERROR = new QuickSand(FAIL,"fail");

    private int state;//0正确创建 1错误创建
    private String msg;
    private JdbcTemplate jdbcTemplate;
    private CRUDContext context;
    private List<ColumnInfo> columnInfoList;

    private QuickSand(CRUDContext context) throws Exception{
        try {
            DatabaseInfo databaseInfo = context.getDatabaseInfo();
            String cacheKey = DataSourceManagement.makeKey(databaseInfo.getUrl(),databaseInfo.getUserName(),databaseInfo.getPassword());
            DataSource dataSource = DataSourceManagement.dataSourceCache.get(cacheKey);
            this.context = context;
            setDataSource(dataSource);
            jdbcTemplate = getJdbcTemplate();
            columnInfoList = getColumns();
            context.getTableInfo().setColumnInfoList(columnInfoList);
            this.state = SUCCESS;
            this.msg = "success";
        } catch (Exception e) {
            LOGGER.debug("create QuickSand error, context:{}",context,e);
            throw new RuntimeException(e.getMessage());
        }
    }

    private QuickSand(int state,String msg){
        this.state = state;
        this.msg = msg;
    }

    public static QuickSand newInstance(CRUDContext context){
        try {
            return new QuickSand(context);
        } catch (Exception e) {
            return new QuickSand(FAIL,e.getMessage());
        }
    }

    public List<ColumnInfo> getColumns() {
        final String tableName = context.getTableInfo().getTableName();
        final String dbName = context.getTableInfo().getDatabaseName();
        return jdbcTemplate.query(DESC_TABLE_SQL, new PreparedStatementSetter() {
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setString(1, tableName);
                ps.setString(2, dbName);
            }
        }, new RowMapper<ColumnInfo>() {
            public ColumnInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                ColumnInfo result = new ColumnInfo();
                result.setTableSchema(rs.getString("TABLE_SCHEMA"));
                result.setTableName(rs.getString("TABLE_NAME"));
                result.setColumnDefault(rs.getString("COLUMN_DEFAULT"));
                result.setColumnName(rs.getString("COLUMN_NAME"));
                result.setIsNullAble(rs.getString("IS_NULLABLE"));
                result.setDataType(rs.getString("DATA_TYPE"));
                result.setColumnKey(rs.getString("COLUMN_KEY"));
                result.setColumnComment(rs.getString("COLUMN_COMMENT"));
                return result;
            }
        });
    }

    public List<BlackJadeKylin> query() throws Exception{
        final List<ColumnInfo> columnInfoList = this.columnInfoList;
        final String sql;
        if (context.getRowBounds() != null) {
            sql = JoinSQL.on(context).selectSQL().withLimit().toString();
        } else {
            sql = JoinSQL.on(context).selectSQL().toString();
        }
        return jdbcTemplate.query(sql, new PreparedStatementSetter() {
            RowBounds rowBounds = context.getRowBounds();

            public void setValues(PreparedStatement ps) throws SQLException {
                if (rowBounds != null) {
                    ps.setInt(1, rowBounds.getOffset());
                    ps.setInt(2, rowBounds.getLimit());
                    LOGGER.debug("qt: {} ,query sql is: {}",context.getRequestId(), sql);
                }
            }
        }, new RowMapper<BlackJadeKylin>() {
            public BlackJadeKylin mapRow(ResultSet rs, int rowNum) throws SQLException {
                BlackJadeKylin kylin = BlackJadeKylin.createKylin(columnInfoList);
                if(kylin.getState() == BlackJadeKylin.FAIL){
                    return kylin;
                }
                try {
                    for (ColumnInfo columnInfo : columnInfoList) {
                        String name = columnInfo.getColumnName();
                        kylin.set(name, rs.getObject(name));
                    }
                    return kylin;
                } catch (Exception e) {
                    LOGGER.debug("执行query失败,context: {},sql: {},resultSet: {},rowNum: {}", context, sql, rs, rowNum, e);
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public List<BlackJadeKylin> queryWithCondition() throws Exception{
        final List<ColumnInfo> columnInfoList = context.getTableInfo().getColumnInfoList();
        final String sql;
        if (context.getRowBounds() != null) {
            sql = JoinSQL.on(context).selectSQL().withCondition().withLimit().toString();
        } else {
            sql = JoinSQL.on(context).selectSQL().withCondition().toString();
        }
        return jdbcTemplate.query(sql, new PreparedStatementSetter() {
            BlackJadeKylin condition = context.getCondition();
            RowBounds rowBounds = context.getRowBounds();
            int count = 1;
            public void setValues(PreparedStatement ps) throws SQLException {
                for (Iterator<String> iterator = condition.getType().keySet().iterator(); iterator.hasNext(); count++) {
                    try {
                        ps.setObject(count, condition.get(iterator.next()));
                    } catch (Exception e) {
                        LOGGER.debug("query condition设值失败，context: {},sql: {}",context,sql);
                        throw new RuntimeException("select with condition sql 设置值失败", e);
                    }
                }
                if (rowBounds != null) {
                    ps.setInt(count, rowBounds.getOffset());
                    ps.setInt(count + 1, rowBounds.getLimit());
                }
                LOGGER.debug("qt: {} ,query with condition sql is: {}",context.getRequestId(), sql);
            }
        }, new RowMapper<BlackJadeKylin>() {
            public BlackJadeKylin mapRow(ResultSet rs, int rowNum) throws SQLException {
                BlackJadeKylin kylin = BlackJadeKylin.createKylin(columnInfoList);
                if(kylin.getState() == BlackJadeKylin.FAIL){
                    return kylin;
                }
                try {
                    for (ColumnInfo columnInfo : columnInfoList) {
                        String name = columnInfo.getColumnName();
                        kylin.set(name, rs.getObject(name));
                    }
                    return kylin;
                } catch (Exception e) {
                    LOGGER.debug("执行query with condition失败,context: {},sql: {},resultSet: {},rowNum: {}", context, sql, rs, rowNum, e);
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public int insert() throws Exception{
        final String sql = JoinSQL.on(context).insertSQL().toString();
        return jdbcTemplate.update(sql, new PreparedStatementSetter() {
            public void setValues(PreparedStatement ps) throws SQLException {
                BlackJadeKylin kylin = context.getKylin();
                Map<String, Class<?>> map = kylin.getType();
                int count = 1;
                for (Iterator<String> iterator = map.keySet().iterator(); iterator.hasNext(); count++) {
                    try {
                        ps.setObject(count, kylin.get(iterator.next()));
                    } catch (Exception e) {
                        LOGGER.debug("insert设值失败,context: {},sql: {}", context, sql,e);
                        throw new RuntimeException("insert sql 设置值失败", e);
                    }
                }
                LOGGER.debug("qt: {} ,insert sql is: {}",context.getRequestId(), sql);
            }
        });
    }

    public int delete() throws Exception{
        final String sql = JoinSQL.on(context).deleteSQL().withCondition().toString();
        return jdbcTemplate.update(sql, new PreparedStatementSetter() {
            BlackJadeKylin condition = context.getCondition();
            int count = 1;

            public void setValues(PreparedStatement ps) throws SQLException {
                for (Iterator<String> iterator = condition.getType().keySet().iterator(); iterator.hasNext(); count++) {
                    try {
                        ps.setObject(count, condition.get(iterator.next()));
                    } catch (Exception e) {
                        LOGGER.debug("delete设值失败,context: {},sql: {}", context, sql,e);
                        throw new RuntimeException("delete with condition sql 设置值失败", e);
                    }
                }
                LOGGER.debug("qt: {} ,delete sql is: {}",context.getRequestId(), sql);
            }
        });
    }

    public synchronized int update() throws Exception{
        final String sql = JoinSQL.on(context).updateSQL().withCondition().toString();
        return jdbcTemplate.update(sql, new PreparedStatementSetter() {
            BlackJadeKylin kylin = context.getKylin();
            BlackJadeKylin condition = context.getCondition();
            int count = 1;
            public void setValues(PreparedStatement ps) throws SQLException {
                for (Iterator<String> iterator = kylin.getType().keySet().iterator(); iterator.hasNext(); count++) {
                    try {
                        ps.setObject(count, kylin.get(iterator.next()));
                    } catch (Exception e) {
                        LOGGER.debug("update设值失败,context: {},sql: {}", context, sql,e);
                        throw new RuntimeException("update sql 设置值失败", e);
                    }
                }
                for (Iterator<String> iterator = condition.getType().keySet().iterator(); iterator.hasNext(); count++) {
                    try {
                        ps.setObject(count, condition.get(iterator.next()));
                    } catch (Exception e) {
                        LOGGER.debug("update condition设值失败,context: {},sql: {}", context, sql,e);
                        throw new RuntimeException("update condition 设置值失败", e);
                    }
                }
                LOGGER.debug("qt: {} ,update sql is: {}",context.getRequestId(), sql);
            }
        });
    }

    public int getState() {
        return state;
    }

    public QuickSand setState(int state) {
        this.state = state;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public QuickSand setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    private static final String HOST = "127.0.0.1";
    private static final String PORT = "3306";
    private static final String URL = "jdbc:mysql://%s:%s?useUnicode=true&amp;characterEncoding=UTF-8&amp;zeroDateTimeBehavior=convertToNull";
    private static final String USERNAME = "nostalie";
    private static final String PASSWORD = "lsroot";

    public static void main(String[] args) throws Exception {
        CRUDContext context = CRUDContext.builder()
                .setHost(HOST)
                .setPort(PORT)
                .setUserName(USERNAME)
                .setPassword(PASSWORD)
                .setURL(String.format(URL,HOST,PORT))
                .setDatabaseName("dbtest")
                .setTableName("user")
                .build();
        QuickSand quickSand = QuickSand.newInstance(context);
        if(quickSand.getState() == QuickSand.SUCCESS) {
            List<BlackJadeKylin> result = quickSand.query();
            System.out.println(result);
        }else {
            System.out.println("failed");
        }
    }
}
