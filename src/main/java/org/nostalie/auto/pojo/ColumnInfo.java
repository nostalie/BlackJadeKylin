package org.nostalie.auto.pojo;

import com.google.common.collect.Lists;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author by nostalie on 17-8-18.
 */
public class ColumnInfo {
    //数据库名
    private String tableSchema;
    //表名
    private String tableName;
    //列名
    private String columnName;
    //列默认值
    private String columnDefault;
    //是否可为null
    private String isNullAble;
    //mysql 数据类型
    private String dataType;
    //PRI 主键
    private String columnKey;
    //字段描述
    private String columnComment;
    //前端展示的名字
    private String displayName;

    public String getTableSchema() {
        return tableSchema;
    }

    public void setTableSchema(String tableSchema) {
        this.tableSchema = tableSchema;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnDefault() {
        return columnDefault;
    }

    public void setColumnDefault(String columnDefault) {
        this.columnDefault = columnDefault;
    }

    public String getIsNullAble() {
        return isNullAble;
    }

    public void setIsNullAble(String isNullAble) {
        this.isNullAble = isNullAble;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getColumnKey() {
        return columnKey;
    }

    public void setColumnKey(String columnKey) {
        this.columnKey = columnKey;
    }

    public String getColumnComment() {
        return columnComment;
    }

    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }

    public String getDisplayName() {
        if (displayName == null) {
            makeDisplayName();
        }
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String makeDisplayName() {
        if (columnComment != null && columnComment.length() <= 10) {
            this.displayName = columnComment;
            return columnComment;
        } else {
            this.displayName = columnName;
            return columnName;
        }
    }

    @Override
    public String toString() {
        return "ColumnInfo{" +
                "tableSchema='" + tableSchema + '\'' +
                ", tableName='" + tableName + '\'' +
                ", columnName='" + columnName + '\'' +
                ", columnDefault='" + columnDefault + '\'' +
                ", isNullAble='" + isNullAble + '\'' +
                ", dataType='" + dataType + '\'' +
                ", columnKey='" + columnKey + '\'' +
                ", columnComment='" + columnComment + '\'' +
                ", displayName='" + displayName + '\'' +
                '}';
    }

    public static class ZK extends ColumnInfo{

    }

    public static void main(String[] args) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ColumnInfo columnInfo = new ColumnInfo();
        ZK zk = new ZK();
        System.out.println(columnInfo.getClass());
        System.out.println(zk.getClass());
        System.out.println(columnInfo.getClass() == zk.getClass());
    }
}
