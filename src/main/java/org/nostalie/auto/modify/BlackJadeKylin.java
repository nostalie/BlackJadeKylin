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
 * @author nostalie on 17-8-15.
 */
@JsonSerialize(using = KylinSerializer.class)
@JsonDeserialize(using = KylinDeserializer.class)
public class BlackJadeKylin {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlackJadeKylin.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String SET = "set";
    private static final String GET = "get";
    public static final int SUCCESS = 0;
    public static final int FAIL = -1;
    private static final String SUCCESS_MSG = "success";

    private static final ClassPool DEFAULT_POOL = ClassPool.getDefault();
    //数据表实际对应的类
    private Object data;
    //key=属性名=columnName value=属性类型
    private Map<String, Class<?>> type = Maps.newLinkedHashMap();
    //key=columnName value=列信息
    private Map<String,ColumnInfo> columnInfoMap = Maps.newHashMap();
    private int state;//0创建成功，1创建失败
    private String msg;

    private BlackJadeKylin(Builder builder) {
        this.data = builder.data;
        this.type = builder.type;
        this.columnInfoMap = builder.columnInfoMap;
        this.state = SUCCESS;
        this.msg = SUCCESS_MSG;
    }

    private BlackJadeKylin(int state, String msg){
        this.state = state;
        this.msg = msg;
    }

    public static BlackJadeKylin createKylin(List<ColumnInfo> columnInfoList){
        try {
            BlackJadeKylin.Builder builder = BlackJadeKylin.builder();
            for(ColumnInfo columnInfo : columnInfoList){
                builder.setField(columnInfo, JavaType.getJavaType(columnInfo.getDataType()));
            }
            return builder.build();
        } catch (Exception e) {
            LOGGER.debug("create blackjadeKylin failed",e);
            return new BlackJadeKylin(FAIL,e.getMessage());
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
                LOGGER.debug("获取实例失败", e);
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
            //从class pool中移除ctClass
            ctClass.detach();
            return new BlackJadeKylin(this);
        }
    }

    public <V> BlackJadeKylin set(String name, V value, Class<? extends V> clazz) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Preconditions.checkNotNull(name);
        if (clazz != null) {
            if (clazz != type.get(name) && clazz.getSuperclass() != type.get(name)) {
                String log = type.get(name) == null ? "null" : type.get(name).getName();
                LOGGER.debug("set方法值类型不匹配 属性类型:{},传入的值类型:{}", log, value.getClass().getName());
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
    public <V> V get(String name, Class<? super V> clazz) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Preconditions.checkNotNull(name);
        if (clazz != null) {
            if (clazz != type.get(name) && clazz != type.get(name).getSuperclass()) {
                String logParam = type.get(name) == null ? "null" : type.get(name).getName();
                LOGGER.debug("查询类型与实际类型不匹配,查询类型：{},属性类型：{}", clazz.getName(), logParam);
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

    public int getState() {
        return state;
    }

    public BlackJadeKylin setState(int state) {
        this.state = state;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public BlackJadeKylin setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public static void main(String[] args) throws NotFoundException, CannotCompileException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        BlackJadeKylin age = BlackJadeKylin.builder().setField("age", int.class).build();
        age.set("age",23);
        System.out.println(age);
    }
}
