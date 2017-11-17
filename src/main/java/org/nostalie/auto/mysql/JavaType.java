package org.nostalie.auto.mysql;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.codehaus.jackson.JsonGenerator;
import org.nostalie.auto.modify.BlackJadeKylin;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.nostalie.auto.modify.KylinUtils.VERTICAL;

/**
 * @author nostalie on 17-8-20.
 */
public enum JavaType {

    INT("int|tinyint", int.class),
    LONG("bigint",long.class),
    STRING("varchar", String.class),
    DATE("timestamp", Date.class),
    DECIMAL("decimal", BigDecimal.class);

    private String mysqlType;
    private Class<?> javaType;
    private static Map<String, JavaType> map = Maps.newHashMap();

    static {
        for (JavaType type : JavaType.values()) {
            String mysqlType = type.mysqlType;
            List<String> types = new ArrayList<String>(VERTICAL.splitToList(mysqlType));
            for (String t : types) {
                map.put(t, type);
            }
        }
    }

    JavaType(String mysqlType, Class<?> javaType) {
        this.mysqlType = mysqlType;
        this.javaType = javaType;
    }

    public String getMysqlType() {
        return mysqlType;
    }

    public static Class<?> getJavaType(String mysqlType) {
        Preconditions.checkNotNull(mysqlType);
        return map.get(mysqlType.toLowerCase()).javaType;
    }

    public static Map<String, JavaType> getMap() {
        return map;
    }
}
