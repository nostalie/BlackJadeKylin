package org.nostalie.auto.modify;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.nostalie.auto.mysql.JavaType;
import org.nostalie.auto.pojo.ColumnInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Created by nostalie on 17-8-15.
 */
@JsonSerialize(using = KylinSerializer.class)
@JsonDeserialize(using = KylinDeserializer.class)
public class BlackJadeKylin {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlackJadeKylin.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String SET = "set";
    private static final String GET = "get";

    private static final ClassPool DEFAULT_POOL = ClassPool.getDefault();
    private Object data;
    //key=属性名=columnName value=属性类型
    private Map<String, Class<?>> type = Maps.newLinkedHashMap();

    //key=columnName value=列信息
    private Map<String,ColumnInfo> columnInfoMap = Maps.newHashMap();

    private BlackJadeKylin(Builder builder) {
        this.data = builder.data;
        this.type = builder.type;
        this.columnInfoMap = builder.columnInfoMap;
    }

    public static BlackJadeKylin createKylin(List<ColumnInfo> columnInfoList){
        try {
            BlackJadeKylin.Builder builder = BlackJadeKylin.builder();
            for(ColumnInfo columnInfo : columnInfoList){
                builder.setField(columnInfo, JavaType.getMap().get(columnInfo.getDataType().toLowerCase()).getJavaType());
            }
            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException("创建BlackJadeKylin失败",e);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private CtClass ctClass;
        private Object data;
        private Map<String, Class<?>> type = Maps.newLinkedHashMap();
        private Map<String,ColumnInfo> columnInfoMap = Maps.newHashMap();

        private Builder() {
            try {
                String name = KylinUtils.getPackageName() + "." + KylinUtils.getUniqName();
                ctClass = DEFAULT_POOL.makeClass(name);
            } catch (Exception e) {
                LOGGER.error("获取实例失败", e);
                throw new RuntimeException("获取实例失败", e);
            }
        }

        public Builder setField(String name, Class<?> clazz) throws NotFoundException, CannotCompileException {
            Preconditions.checkNotNull(clazz);
            Preconditions.checkNotNull(name);

            CtField ctField = new CtField(DEFAULT_POOL.get(clazz.getName()), name, ctClass);
            ctField.setModifiers(Modifier.PRIVATE);

            ctClass.addMethod(CtNewMethod.setter(SET + KylinUtils.firstUpper(name), ctField));
            ctClass.addMethod(CtNewMethod.getter(GET + KylinUtils.firstUpper(name), ctField));
            ctClass.addField(ctField);
            type.put(name, clazz);
            return this;
        }

        public Builder setField(ColumnInfo columnInfo,Class<?> clazz) throws NotFoundException, CannotCompileException {
            String name = columnInfo.getColumnName();
            setField(name,clazz);
            columnInfoMap.put(name,columnInfo);
            return this;
        }

        public BlackJadeKylin build() throws CannotCompileException, IllegalAccessException, InstantiationException {
            data = ctClass.toClass().newInstance();
            return new BlackJadeKylin(this);
        }
    }

    public <V> BlackJadeKylin set(String name, V value, Class<? extends V> clazz) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Preconditions.checkNotNull(name);
        if (clazz != null) {
            if (clazz != type.get(name)) {
                String log = type.get(name) == null ? "null" : type.get(name).getName();
                LOGGER.error("set方法值类型不匹配 属性类型:{},传入的值类型:{}", log, value.getClass().getName());
                throw new RuntimeException("set 方法 值类型不匹配");
            }
        }
        String setStr = SET + KylinUtils.firstUpper(name);
        Method setMethod = data.getClass().getMethod(setStr, type.get(name));
        setMethod.invoke(data, value);
        return this;
    }

    public <V> BlackJadeKylin set(String name,V value) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return set(name,value,null);
    }

    @SuppressWarnings("unchecked")
    public <V> V get(String name, Class<? extends V> clazz) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Preconditions.checkNotNull(name);
        if (clazz != null) {
            if (clazz != type.get(name)) {
                String logParam = type.get(name) == null ? "null" : type.get(name).getName();
                LOGGER.error("查询类型与实际类型不匹配,查询类型：{},属性类型：{}", clazz.getName(), logParam);
                throw new RuntimeException("查询类型与实际类型不匹配");
            }
        }
        String getStr = GET + KylinUtils.firstUpper(name);
        Method getMethod = data.getClass().getMethod(getStr);
        return (V) getMethod.invoke(data);
    }

    public <V> V get(String name) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return get(name, null);
    }

    public Map<String, Class<?>> getType() {
        return type;
    }

    public Map<String, ColumnInfo> getColumnInfoMap() {
        return columnInfoMap;
    }

    @Override
    public String toString() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (IOException e) {
            return "toString 失败原因:" + e.getMessage();
        }
    }
}
