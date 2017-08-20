package org.nostalie.auto.pojo;

import org.nostalie.auto.modify.BlackJadeKylin;

/**
 * Created by nostalie on 17-8-18.
 */
public class CRUDContext {
    private String requestId;
    private DatabaseInfo databaseInfo;
    private TableInfo tableInfo;
    private RowBounds rowBounds;
    /**
     * insert sql 需要的插入实例
     * delete sql 不需要此字段
     * update sql 需要的set k=v 实例
     * select sql 不需要此字段
     */
    private BlackJadeKylin kylin;
    /**
     * insert sql 不需要此字段
     * delete sql 拼接where 条件字段
     * update sql 拼接where 条件字段
     * select sql 拼接where 条件字段
     */
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

    public BlackJadeKylin getKylin() {
        return kylin;
    }

    public void setKylin(BlackJadeKylin kylin) {
        this.kylin = kylin;
    }

    public BlackJadeKylin getCondition() {
        return condition;
    }

    public void setCondition(BlackJadeKylin condition) {
        this.condition = condition;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "CRUDContext{" +
                "requestId='" + requestId + '\'' +
                ", databaseInfo=" + databaseInfo +
                ", tableInfo=" + tableInfo +
                ", rowBounds=" + rowBounds +
                ", kylin=" + kylin +
                ", condition=" + condition +
                '}';
    }
}
