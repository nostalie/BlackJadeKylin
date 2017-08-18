package org.nostalie.auto.mysql;

import com.google.common.base.Preconditions;
import org.nostalie.auto.modify.BlackJadeKylin;
import org.nostalie.auto.modify.KylinUtils;
import org.nostalie.auto.pojo.ColumnInfo;
import org.nostalie.auto.pojo.QueryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Created by nostalie on 17-8-18.
 */
public class JoinSQL {

    private static final Logger LOGGER = LoggerFactory.getLogger(JoinSQL.class);

    private static final String EMPTY = "";
    private static final String SPACE = " ";
    private static final String SIGN = "=";
    private static final String AND = "and";
    private static final String BASE_SELECT = "select %s from %s";
    private static final String BASE_INSERT = "insert into %s(%s) values(%s)";
    private static final String BASE_DELETE = "delete from %s where (%s)";
    private static final String BASE_UPDATE = "update %s set %s";

    public static String selectSQL(List<ColumnInfo> columnInfoList, QueryContext context) {
        Preconditions.checkNotNull(columnInfoList);
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(context.getTableInfo());
        if(KylinUtils.nullToEmpty(context.getTableInfo().getDatabaseName()).equals(EMPTY)
                || KylinUtils.nullToEmpty(context.getTableInfo().getTableName()).equals(EMPTY)){
            LOGGER.error("数据库名称/表名 不能为空,context: {}",context);
            throw new RuntimeException("数据库或表明为空");
        }
        StringBuilder sb = new StringBuilder();
        for (ColumnInfo columnInfo : columnInfoList) {
            sb.append(columnInfo.getColumnName())
                    .append(",");
        }
        sb.setLength(sb.length() - 1);
        String resultSet = sb.toString();
        sb.setLength(0);
        sb.append(context.getTableInfo().getDatabaseName())
                .append(".")
                .append(context.getTableInfo().getTableName());
        String tableName = sb.toString();
        sb.setLength(0);
        return String.format(BASE_SELECT, resultSet, tableName);
    }

    public static String selectWithConditionSQL(List<ColumnInfo> columnInfoList, QueryContext context) {
        Preconditions.checkNotNull(context.getCondition());
        String result = selectSQL(columnInfoList,context);
        try {
            BlackJadeKylin condition = context.getCondition();
            Set<String> columnNames = condition.getMap().keySet();
            StringBuilder sb = new StringBuilder();
            sb.append(result)
                    .append(SPACE)
                    .append("where")
                    .append(SPACE);
            for(String name : columnNames){
                sb.append(name)
                        .append(SIGN)
                        .append(condition.get(name))
                        .append(SPACE)
                        .append(AND)
                        .append(SPACE);
            }
            sb.setLength(sb.length() - AND.length() - SPACE.length());
            return sb.toString();
        } catch (Exception e) {
            LOGGER.error("拼接select condition SQL失败, columnInfo List:{},QueryContext:{}",columnInfoList,context,e);
            throw new RuntimeException("拼接select condition SQL失败",e);
        }
    }

    public static String selectConditionLimitSQL(List<ColumnInfo> columnInfoList,QueryContext context){
        String result = selectWithConditionSQL(columnInfoList,context);
        Preconditions.checkNotNull(context.getRowBounds());
        return result +
                " " +
                "limit" +
                context.getRowBounds().getOffset() +
                "," +
                context.getRowBounds().getLimit();
    }
    public static void main(String[] args) {
        System.out.println(String.format(BASE_SELECT, "zhangjian"));
    }
}
