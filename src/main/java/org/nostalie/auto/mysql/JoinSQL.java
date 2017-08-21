package org.nostalie.auto.mysql;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.codehaus.jackson.map.ObjectMapper;
import org.nostalie.auto.modify.BlackJadeKylin;
import org.nostalie.auto.modify.KylinUtils;
import org.nostalie.auto.pojo.ColumnInfo;
import org.nostalie.auto.pojo.CRUDContext;
import org.nostalie.auto.pojo.DatabaseInfo;
import org.nostalie.auto.pojo.RowBounds;
import org.nostalie.auto.pojo.TableInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by nostalie on 17-8-18.
 */
public class JoinSQL {

    private static final Logger LOGGER = LoggerFactory.getLogger(JoinSQL.class);

    public static final String EMPTY = "";
    private static final String SPACE = " ";
    private static final String SIGN = "=";
    private static final String COMMA = ",";
    private static final String SPOT = ".";
    private static final String QUESTION_MARK = "?";
    private static final String AND = "and";
    private static final String WHERE = "where";
    private static final String LIMIT = "limit";
    private static final String BASE_INSERT = "insert into %s(%s) values(%s)";
    private static final String BASE_DELETE = "delete from %s";
    private static final String BASE_UPDATE = "update %s set %s";
    private static final String BASE_SELECT = "select %s from %s";

    private String sql;
    private CRUDContext context;

    private JoinSQL(CRUDContext context) {
        this.context = context;
    }

    public static  JoinSQL on(CRUDContext context){
        return new JoinSQL(context);
    }

    public JoinSQL selectSQL() {
        try {
            verifyContext(context);
            List<ColumnInfo> columnInfoList = context.getTableInfo().getColumnInfoList();
            Preconditions.checkNotNull(columnInfoList,"列信息不能为空");
            StringBuilder sb = new StringBuilder();
            for (ColumnInfo columnInfo : columnInfoList) {
                sb.append(columnInfo.getColumnName())
                        .append(COMMA);
            }
            sb.setLength(sb.length() - 1);
            String resultSet = sb.toString();
            sb.setLength(0);
            sb.append(context.getTableInfo().getDatabaseName())
                    .append(SPOT)
                    .append(context.getTableInfo().getTableName());
            String tableName = sb.toString();
            sb.setLength(0);
            sql = String.format(BASE_SELECT, resultSet, tableName);
            return this;
        } catch (Exception e) {
            LOGGER.error("拼接select SQL失败, CRUDContext:{}", context, e);
            throw new RuntimeException("拼接select SQL失败", e);
        }
    }

    public JoinSQL insertSQL(){
        try {
            verifyContext(context);
            Preconditions.checkNotNull(context.getKylin(),"insert 语句必须有插入实例,kylin 不能为空");
            Preconditions.checkArgument(!KylinUtils.isEmpty(context.getKylin().getType()),"构建insert语句，插入字段不能为空");
            String tableName = context.getTableInfo().getDatabaseName() + SPOT + context.getTableInfo().getTableName();
            BlackJadeKylin kylin = context.getKylin();
            Set<String> columnNames = kylin.getType().keySet();
            StringBuilder key = new StringBuilder();
            StringBuilder value = new StringBuilder();
            for(String name : columnNames){
                key.append(name)
                        .append(COMMA);

                value.append(QUESTION_MARK)
                        .append(COMMA);
            }
            key.setLength(key.length() - COMMA.length());
            value.setLength(value.length() - COMMA.length());
            sql = String.format(BASE_INSERT,tableName,key.toString(),value.toString());
            return this;
        } catch (Exception e) {
            LOGGER.error("拼接insert SQL失败,CRUDContext: {}", context, e);
            throw new RuntimeException("拼接insert SQL失败", e);
        }
    }

    public JoinSQL deleteSQL(){
        try{
            verifyContext(context);
            String tableName = context.getTableInfo().getDatabaseName() + SPOT + context.getTableInfo().getTableName();
            sql = String.format(BASE_DELETE,tableName);
            return this;
        }catch (Exception e){
            LOGGER.error("拼接delete SQL失败,CRUDContext:{}", context, e);
            throw new RuntimeException("拼接delete SQL失败", e);
        }
    }

    public JoinSQL updateSQL(){
        try {
            verifyContext(context);
            Preconditions.checkNotNull(context.getKylin(),"update 语句必须有更新实例,kylin 不能为空");
            Preconditions.checkArgument(!KylinUtils.isEmpty(context.getKylin().getType()),"构建update语句，更新字段不能为空");
            String tableName = context.getTableInfo().getDatabaseName() + SPOT + context.getTableInfo().getTableName();
            BlackJadeKylin kylin = context.getKylin();
            Set<String> columnNames = kylin.getType().keySet();
            StringBuilder sb = new StringBuilder();
            for(String name : columnNames){
                sb.append(name)
                        .append(SIGN)
                        .append(QUESTION_MARK)
                        .append(COMMA);
            }
            sb.setLength(sb.length() - COMMA.length());
            sql = String.format(BASE_UPDATE,tableName,sb.toString());
            return this;
        } catch (Exception e) {
            LOGGER.error("拼接update SQL失败, CRUDContext:{}", context, e);
            throw new RuntimeException("拼接update SQL失败", e);
        }
    }

    public JoinSQL withCondition() {
        try {
            Preconditions.checkNotNull(context.getCondition(),"进行条件拼接，则condition字段不能为空");
            Preconditions.checkArgument(!KylinUtils.isEmpty(context.getCondition().getType()),"构建where语句，条件字段不能为空");
            BlackJadeKylin condition = context.getCondition();
            Set<String> columnNames = condition.getType().keySet();
            StringBuilder sb = new StringBuilder();
            sb.append(this.sql)
                    .append(SPACE)
                    .append(WHERE)
                    .append(SPACE);
            for (String name : columnNames) {
                sb.append(name)
                        .append(SIGN)
                        .append(QUESTION_MARK)
                        .append(SPACE)
                        .append(AND)
                        .append(SPACE);
            }
            sb.setLength(sb.length() - AND.length() - SPACE.length());
            this.sql = sb.toString();
            return this;
        } catch (Exception e) {
            LOGGER.error("拼接condition SQL失败,CRUDContext:{}",context, e);
            throw new RuntimeException("拼接condition SQL失败", e);
        }
    }

    public JoinSQL withLimit() {
        try {
            Preconditions.checkNotNull(context.getRowBounds(),"拼接limit，则RowBounds字段不能为空");
            this.sql += SPACE + LIMIT + SPACE + QUESTION_MARK + COMMA + QUESTION_MARK;
            return this;
        } catch (Exception e) {
            LOGGER.error("拼接limit SQL失败, CRUDContext:{}",context, e);
            throw new RuntimeException("拼接limit SQL失败", e);
        }
    }

    private void verifyContext(CRUDContext context){
        Preconditions.checkNotNull(context,"拼接sql 上下文信息不能为空");
        Preconditions.checkNotNull(context.getTableInfo(),"table 信息不能为空");
        Preconditions.checkArgument(!KylinUtils.nullToEmpty(context.getTableInfo().getDatabaseName()).equals(EMPTY),"拼接sql数据库名不能为空");
        Preconditions.checkArgument(!KylinUtils.nullToEmpty(context.getTableInfo().getTableName()).equals(EMPTY),"拼接sql表名不能为空");
    }

    @Override
    public String toString() {
        return this.sql;
    }

    private static final String HOST = "127.0.0.1";
    private static final String PORT = "3306";
    private static final String URL = "jdbc:mysql://%s:%s?useUnicode=true&amp;characterEncoding=UTF-8&amp;zeroDateTimeBehavior=convertToNull";
    private static final String USERNAME = "nostalie";
    private static final String PASSWORD = "lsroot";

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DatabaseInfo databaseInfo = new DatabaseInfo();
        databaseInfo.setHost(HOST);
        databaseInfo.setPort(PORT);
        databaseInfo.setUserName(USERNAME);
        databaseInfo.setPassword(PASSWORD);
        databaseInfo.setUrl(String.format(URL, HOST, PORT));
        TableInfo tableInfo = new TableInfo();
        tableInfo.setTableName("kylin_t");
        tableInfo.setDatabaseName("dbtest");
        CRUDContext context = new CRUDContext();
        context.setDatabaseInfo(databaseInfo);
        context.setTableInfo(tableInfo);

        QuickSand quickSand = new QuickSand(context);
        List<ColumnInfo> columns = quickSand.getColumns();
        System.out.println("columns is: " + mapper.writeValueAsString(columns));
        context.getTableInfo().setColumnInfoList(columns);

        RowBounds rowBounds = new RowBounds(0, 5);
        context.setRowBounds(rowBounds);

        BlackJadeKylin kylin = BlackJadeKylin.builder()
                .setField("create_time",Date.class)
                .setField("id",int.class)
                .setField("name",String.class)
                .setField("age",int.class)
                .setField("salary", BigDecimal.class).build();
        kylin.set("create_time",new Date())
                .set("id",3)
                .set("name","zhangjian")
                .set("age",23)
                .set("salary",new BigDecimal("123.12343"));

        BlackJadeKylin condition = BlackJadeKylin.builder().setField("id", int.class).build();
        condition.set("id", 3, int.class);
        context.setKylin(kylin);
        context.setCondition(condition);

        //int query = quickSand.delete(context);
        List<BlackJadeKylin> kylins = quickSand.queryWithCondition();
        System.out.println(kylins);
    }
}
