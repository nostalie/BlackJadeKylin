package org.nostalie.auto.pojo;

import org.nostalie.auto.modify.BlackJadeKylin;

/**
 * Created by nostalie on 17-8-18.
 */
public class QueryContext {
    private DatabaseInfo databaseInfo;
    private TableInfo tableInfo;
    private RowBounds rowBounds;
    private BlackJadeKylin condition;

    public DatabaseInfo getDatabaseInfo() {
        return databaseInfo;
    }

    public void setDatabaseInfo(DatabaseInfo databaseInfo) {
        this.databaseInfo = databaseInfo;
    }

    public TableInfo getTableInfo() {
        return tableInfo;
    }

    public void setTableInfo(TableInfo tableInfo) {
        this.tableInfo = tableInfo;
    }

    public RowBounds getRowBounds() {
        return rowBounds;
    }

    public void setRowBounds(RowBounds rowBounds) {
        this.rowBounds = rowBounds;
    }

    public BlackJadeKylin getCondition() {
        return condition;
    }

    public void setCondition(BlackJadeKylin condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "QueryContext{" +
                "databaseInfo=" + databaseInfo +
                ", tableInfo=" + tableInfo +
                ", rowBounds=" + rowBounds +
                ", condition=" + condition +
                '}';
    }
}
