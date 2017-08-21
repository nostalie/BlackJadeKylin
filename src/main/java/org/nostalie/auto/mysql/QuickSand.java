package org.nostalie.auto.mysql;

import org.nostalie.auto.modify.BlackJadeKylin;
import org.nostalie.auto.modify.KylinUtils;
import org.nostalie.auto.pojo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by nostalie on 17-8-18.
 */
public class QuickSand extends JdbcDaoSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuickSand.class);

    private static final String DESC_TABLE_SQL = "select TABLE_SCHEMA,TABLE_NAME,COLUMN_NAME,COLUMN_DEFAULT,IS_NULLABLE,DATA_TYPE,COLUMN_KEY,COLUMN_COMMENT " +
            "from information_schema.columns where table_name=? and table_schema=?";

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private CRUDContext context;

    public QuickSand(CRUDContext context) {
        DatabaseInfo databaseInfo = context.getDatabaseInfo();
        dataSource = new DriverManagerDataSource(databaseInfo.getUrl(), databaseInfo.getUserName(), databaseInfo.getPassword());
        this.context = context;
        setDataSource(dataSource);
        jdbcTemplate = getJdbcTemplate();
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

    public List<BlackJadeKylin> query() {
        final List<ColumnInfo> columnInfoList = context.getTableInfo().getColumnInfoList();
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
                    LOGGER.info("qt: {} ,query sql is: {}",context.getRequestId(), sql);
                }
            }
        }, new RowMapper<BlackJadeKylin>() {
            public BlackJadeKylin mapRow(ResultSet rs, int rowNum) throws SQLException {
                BlackJadeKylin kylin = BlackJadeKylin.createKylin(columnInfoList);
                try {
                    for (ColumnInfo columnInfo : columnInfoList) {
                        String name = columnInfo.getColumnName();
                        kylin.set(name, rs.getObject(name));
                    }
                    return kylin;
                } catch (Exception e) {
                    LOGGER.error("执行query失败,context: {},sql: {},resultSet: {},rowNum: {}", context, sql, rs, rowNum, e);
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public List<BlackJadeKylin> queryWithCondition() {
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
                        LOGGER.error("query condition设值失败，context: {},sql: {}",context,sql);
                        throw new RuntimeException("select with condition sql 设置值失败", e);
                    }
                }
                if (rowBounds != null) {
                    ps.setInt(count, rowBounds.getOffset());
                    ps.setInt(count + 1, rowBounds.getLimit());
                }
                LOGGER.info("qt: {} ,query with condition sql is: {}",context.getRequestId(), sql);
            }
        }, new RowMapper<BlackJadeKylin>() {
            public BlackJadeKylin mapRow(ResultSet rs, int rowNum) throws SQLException {
                BlackJadeKylin kylin = BlackJadeKylin.createKylin(columnInfoList);
                try {
                    for (ColumnInfo columnInfo : columnInfoList) {
                        String name = columnInfo.getColumnName();
                        kylin.set(name, rs.getObject(name));
                    }
                    return kylin;
                } catch (Exception e) {
                    LOGGER.error("执行query with condition失败,context: {},sql: {},resultSet: {},rowNum: {}", context, sql, rs, rowNum, e);
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public int insert() {
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
                        LOGGER.error("insert设值失败,context: {},sql: {}", context, sql,e);
                        throw new RuntimeException("insert sql 设置值失败", e);
                    }
                }
                LOGGER.info("qt: {} ,insert sql is: {}",context.getRequestId(), sql);
            }
        });
    }

    public int delete() {
        final String sql = JoinSQL.on(context).deleteSQL().withCondition().toString();
        return jdbcTemplate.update(sql, new PreparedStatementSetter() {
            BlackJadeKylin condition = context.getCondition();
            int count = 1;

            public void setValues(PreparedStatement ps) throws SQLException {
                for (Iterator<String> iterator = condition.getType().keySet().iterator(); iterator.hasNext(); count++) {
                    try {
                        ps.setObject(count, condition.get(iterator.next()));
                    } catch (Exception e) {
                        LOGGER.error("delete设值失败,context: {},sql: {}", context, sql,e);
                        throw new RuntimeException("delete with condition sql 设置值失败", e);
                    }
                }
                LOGGER.info("qt: {} ,delete sql is: {}",context.getRequestId(), sql);
            }
        });
    }

    public int update() {
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
                        LOGGER.error("update设值失败,context: {},sql: {}", context, sql,e);
                        throw new RuntimeException("update sql 设置值失败", e);
                    }
                }
                for (Iterator<String> iterator = condition.getType().keySet().iterator(); iterator.hasNext(); count++) {
                    try {
                        ps.setObject(count, condition.get(iterator.next()));
                    } catch (Exception e) {
                        LOGGER.error("update condition设值失败,context: {},sql: {}", context, sql,e);
                        throw new RuntimeException("update condition 设置值失败", e);
                    }
                }
                LOGGER.info("qt: {} ,update sql is: {}",context.getRequestId(), sql);
            }
        });
    }

    private static final String HOST = "127.0.0.1";
    private static final String PORT = "3306";
    private static final String URL = "jdbc:mysql://%s:%s?useUnicode=true&amp;characterEncoding=UTF-8&amp;zeroDateTimeBehavior=convertToNull";
    private static final String USERNAME = "nostalie";
    private static final String PASSWORD = "lsroot";

    public static void main(String[] args) throws Exception {
        DatabaseInfo databaseInfo = new DatabaseInfo();
        databaseInfo.setHost(HOST);
        databaseInfo.setPort(PORT);
        databaseInfo.setUserName(USERNAME);
        databaseInfo.setPassword(PASSWORD);
        databaseInfo.setUrl(String.format(URL, HOST, PORT));
        TableInfo tableInfo = new TableInfo();
        tableInfo.setTableName("user");
        tableInfo.setDatabaseName("personnel_management");
        CRUDContext context = new CRUDContext();
        context.setDatabaseInfo(databaseInfo);
        context.setTableInfo(tableInfo);

        QuickSand quickSand = new QuickSand(context);

        RowBounds rowBounds = new RowBounds(0, 5);
        context.setRowBounds(rowBounds);

        BlackJadeKylin kylin = BlackJadeKylin.builder().setField("gender", int.class)
                .setField("real_name", String.class)
                .setField("user_name", String.class)
                .setField("birthday", Date.class)
                .setField("password", String.class)
                .setField("department_id", int.class)
                .setField("position_id", int.class)
                .setField("create_time", Date.class)
                .setField("update_time", Date.class).build();
        kylin.set("gender", 0, int.class)
                .set("real_name", "基地及你", String.class)
                .set("user_name", "fjeijfie", String.class)
                .set("birthday", new Date(), Date.class)
                .set("password", "12345456", String.class)
                .set("department_id", 1, int.class)
                .set("position_id", 1, int.class)
                .set("create_time", new Date(), Date.class)
                .set("update_time", new Date(), Date.class);

        BlackJadeKylin condition = BlackJadeKylin.builder().setField("id", int.class).build();
        condition.set("id", 49, int.class);
        context.setKylin(kylin);
        context.setCondition(condition);
        int query = quickSand.update();
        //List<BlackJadeKylin> kylins = quickSand.query(context);
       // System.out.println("size" + kylins.size());
        //       System.out.println(query.size());
        //System.out.println(query);
        //System.out.println(quickSand.getColumns("user", "personnel_management"));
    }
}
