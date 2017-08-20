package org.nostalie.auto.mysql;

import com.google.common.collect.Maps;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.nostalie.auto.modify.KylinUtils.VERTICAL;

/**
 * Created by nostalie on 17-8-20.
 */
public enum JavaType {

    INT("int|tinyint",int.class){
        @Override
        @SuppressWarnings("unchecked")
        public Integer get(ResultSet resultSet, String name) throws SQLException {
            return  resultSet.getInt(name);
        }
    },
    STRING("varchar",String.class){
        @Override
        @SuppressWarnings("unchecked")
        public String get(ResultSet resultSet, String name) throws SQLException {
            return resultSet.getString(name);
        }
    },
    DATE("timestamp", Date.class){
        @Override
        @SuppressWarnings("unchecked")
        public Date get(ResultSet resultSet, String name) throws SQLException {
            return resultSet.getDate(name);
        }
    },
    DECIMAL("decimal", BigDecimal.class){
        @Override
        @SuppressWarnings("unchecked")
        public BigDecimal get(ResultSet resultSet, String name) throws SQLException {
            return resultSet.getBigDecimal(name);
        }
    };

    private String mysqlType;
    private Class<?> javaType;
    private static Map<String,JavaType> map = Maps.newHashMap();

    static {
        for(JavaType type : JavaType.values()){
            String mysqlType = type.mysqlType;
            List<String> types = new ArrayList<String>(VERTICAL.splitToList(mysqlType));
            for(String t : types) {
                map.put(t, type);
            }
        }
    }

    abstract public <V> V get(ResultSet resultSet, String name) throws SQLException;

    JavaType(String mysqlType,Class<?> javaType){
        this.mysqlType = mysqlType;
        this.javaType = javaType;
    }

    public String getMysqlType() {
        return mysqlType;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public static Map<String, JavaType> getMap() {
        return map;
    }
}
