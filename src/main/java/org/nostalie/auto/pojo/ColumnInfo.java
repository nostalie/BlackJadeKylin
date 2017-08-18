package org.nostalie.auto.pojo;

/**
 * Created by nostalie on 17-8-18.
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
                '}';
    }
}
