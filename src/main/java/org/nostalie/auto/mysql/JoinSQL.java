package org.nostalie.auto.mysql;

import com.google.common.base.Preconditions;
import org.nostalie.auto.modify.BlackJadeKylin;
import org.nostalie.auto.modify.KylinUtils;
import org.nostalie.auto.pojo.ColumnInfo;
import org.nostalie.auto.pojo.CRUDContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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
            KylinUtils.verifyContext(context);
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
            KylinUtils.verifyContext(context);
            Preconditions.checkNotNull(context.getKylin(),"insert 语句必须有插入实例,kylin 不能为空");
            Preconditions.checkArgument(!KylinUtils.isEmpty(context.getKylin().getMap()),"构建insert语句，插入字段不能为空");
            String tableName = context.getTableInfo().getDatabaseName() + SPOT + context.getTableInfo().getTableName();
            BlackJadeKylin kylin = context.getKylin();
            Set<String> columnNames = kylin.getMap().keySet();
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
            KylinUtils.verifyContext(context);
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
            KylinUtils.verifyContext(context);
            Preconditions.checkNotNull(context.getKylin(),"update 语句必须有更新实例,kylin 不能为空");
            Preconditions.checkArgument(!KylinUtils.isEmpty(context.getKylin().getMap()),"构建update语句，更新字段不能为空");
            String tableName = context.getTableInfo().getDatabaseName() + SPOT + context.getTableInfo().getTableName();
            BlackJadeKylin kylin = context.getKylin();
            Set<String> columnNames = kylin.getMap().keySet();
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
            Preconditions.checkArgument(!KylinUtils.isEmpty(context.getCondition().getMap()),"构建where语句，条件字段不能为空");
            BlackJadeKylin condition = context.getCondition();
            Set<String> columnNames = condition.getMap().keySet();
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

    @Override
    public String toString() {
        return this.sql;
    }
}
