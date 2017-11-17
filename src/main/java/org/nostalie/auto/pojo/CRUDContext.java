package org.nostalie.auto.pojo;

import org.nostalie.auto.modify.BlackJadeKylin;
import org.nostalie.auto.mysql.QuickSand;

/**
 * 初始化需要 Host Port UserName Password DatabaseName TableName(必要参数)
 * Kylin Condition RequestId RowBounds(非必要参数)
 *
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

    private QuickSand quickSand;

    private CRUDContext(){}

    private CRUDContext (Builder builder){
        this.requestId = builder.requestId;
        this.databaseInfo = builder.databaseInfo;
        this.tableInfo = builder.tableInfo;
        this.rowBounds = builder.rowBounds;
        this.condition = builder.condition;
        this.kylin = builder.kylin;
    }

    public static Builder builder(){
        return new Builder();
    }
    public static class Builder{
        private String requestId;
        private DatabaseInfo databaseInfo;
        private TableInfo tableInfo;
        private RowBounds rowBounds;
        private BlackJadeKylin condition;
        private BlackJadeKylin kylin;

        private Builder(){
            this.databaseInfo = new DatabaseInfo();
            this.tableInfo = new TableInfo();
        }

        public Builder setDatabaseId(int databaseId){
            this.databaseInfo.setDatabaseId(databaseId);
            return this;
        }
        public Builder setHost(String host){
            this.databaseInfo.setHost(host);
            return this;
        }
        public Builder setPort(String port){
            this.databaseInfo.setPort(port);
            return this;
        }
        public Builder setUserName(String userName){
            this.databaseInfo.setUserName(userName);
            return this;
        }
        public Builder setPassword(String password){
            this.databaseInfo.setPassword(password);
            return this;
        }
        public Builder setURL(String url){
            this.databaseInfo.setUrl(url);
            return this;
        }
        public Builder setDatabaseName(String databaseName){
            this.tableInfo.setDatabaseName(databaseName);
            return this;
        }
        public Builder setTableName(String tableName){
            this.tableInfo.setTableName(tableName);
            return this;
        }
        public Builder setTableId(int tableId){
            this.tableInfo.setTableId(tableId);
            return this;
        }
        public Builder setRequestId(String requestId){
            this.requestId = requestId;
            return this;
        }
        public Builder setKylin(BlackJadeKylin kylin){
            this.kylin = kylin;
            return this;
        }
        public Builder setCondition(BlackJadeKylin condition){
            this.condition = condition;
            return this;
        }
        public Builder setRowBounds(RowBounds rowBounds){
            this.rowBounds = rowBounds;
            return this;
        }
        public CRUDContext build(){
            return new CRUDContext(this);
        }
    }

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

    public QuickSand getQuickSand() {
        return quickSand;
    }

    public void setQuickSand(QuickSand quickSand) {
        this.quickSand = quickSand;
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
                ", quickSand=" + quickSand +
                '}';
    }
}
