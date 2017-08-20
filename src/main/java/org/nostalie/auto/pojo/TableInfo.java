package org.nostalie.auto.pojo;

import java.util.List;

/**
 * Created by nostalie on 17-8-18.
 */
public class TableInfo {
    private String databaseName;
    private String tableName;
    private List<ColumnInfo> columnInfoList;

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getTableName() {
        return tableName;
    }

    public List<ColumnInfo> getColumnInfoList() {
        return columnInfoList;
    }

    public void setColumnInfoList(List<ColumnInfo> columnInfoList) {
        this.columnInfoList = columnInfoList;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String toString() {
        return "TableInfo{" +
                "databaseName='" + databaseName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", columnInfoList=" + columnInfoList +
                '}';
    }
}
