package org.nostalie.auto.mysql;

import org.nostalie.auto.modify.BlackJadeKylin;
import org.nostalie.auto.pojo.ColumnInfo;
import org.nostalie.auto.pojo.DatabaseInfo;
import org.nostalie.auto.pojo.QueryContext;
import org.nostalie.auto.pojo.TableInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * Created by nostalie on 17-8-18.
 */
public class QuickSand extends JdbcDaoSupport {

    private static final String DESC_TABLE_SQL = "select TABLE_SCHEMA,TABLE_NAME,COLUMN_NAME,COLUMN_DEFAULT,IS_NULLABLE,DATA_TYPE,COLUMN_KEY,COLUMN_COMMENT " +
            "from information_schema.columns where table_name=? and table_schema=?";

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    public QuickSand(QueryContext context) {
        DatabaseInfo databaseInfo = context.getDatabaseInfo();
        dataSource = new DriverManagerDataSource(databaseInfo.getUrl(), databaseInfo.getUserName(), databaseInfo.getPassword());
    }

    {
        setDataSource(dataSource);
        jdbcTemplate = getJdbcTemplate();
    }

    public List<ColumnInfo> getColumns(final String tableName, final String dbName) {
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

    private static final String HOST = "127.0.0.1";
    private static final String PORT = "3306";
    private static final String URL = "jdbc:mysql://%s:%s?useUnicode=true&amp;characterEncoding=UTF-8&amp;zeroDateTimeBehavior=convertToNull";
    private static final String USERNAME = "nostalie";
    private static final String PASSWORD = "lsroot";
    public static void main(String[] args) {
        DatabaseInfo databaseInfo = new DatabaseInfo();
        databaseInfo.setHost(HOST);
        databaseInfo.setPort(PORT);
        databaseInfo.setUserName(USERNAME);
        databaseInfo.setPassword(PASSWORD);
        databaseInfo.setUrl(String.format(URL,HOST,PORT));
        TableInfo tableInfo = new TableInfo();
        tableInfo.setTableName("kylin_t");
        tableInfo.setDatabaseName("dbtest");
        QueryContext context = new QueryContext();
        context.setDatabaseInfo(databaseInfo);
        context.setTableInfo(tableInfo);

        QuickSand quickSand = new QuickSand(context);
        System.out.println(quickSand.getColumns("kylin_t", "dbtest"));
    }
}
