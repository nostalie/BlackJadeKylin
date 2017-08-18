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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by nostalie on 17-8-15.
 */
public class BlackJadeKylin {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlackJadeKylin.class);
    private static final String SET = "set";
    private static final String GET = "get";

    private static final ClassPool DEFAULT_POOL = ClassPool.getDefault();
    private Object data;
    //key=属性名 value=属性类型
    private Map<String, Class<?>> map = Maps.newHashMap();

    private BlackJadeKylin(Builder builder) {
        this.data = builder.data;
        this.map = builder.map;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private CtClass ctClass;
        private Object data;
        private Map<String, Class<?>> map = Maps.newConcurrentMap();

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
            map.put(name, clazz);
            return this;
        }

        public BlackJadeKylin build() throws CannotCompileException, IllegalAccessException, InstantiationException {
            data = ctClass.toClass().newInstance();
            return new BlackJadeKylin(this);
        }
    }

    public <V> BlackJadeKylin set(String name, V value, Class<V> clazz) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Preconditions.checkNotNull(name);
        if (clazz != null) {
            if (clazz != map.get(name)) {
                String log = map.get(name) == null ? "null" : map.get(name).getName();
                LOGGER.error("set方法值类型不匹配 属性类型:{},传入的值类型:{}", log, value.getClass().getName());
                throw new RuntimeException("set 方法 值类型不匹配");
            }
        }
        String setStr = SET + KylinUtils.firstUpper(name);
        Method setMethod = data.getClass().getMethod(setStr, map.get(name));
        setMethod.invoke(data, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <V> V get(String name, Class<V> clazz) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Preconditions.checkNotNull(name);
        if (clazz != null) {
            if (clazz != map.get(name)) {
                String logParam = map.get(name) == null ? "null" : map.get(name).getName();
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

    public Map<String, Class<?>> getMap() {
        return map;
    }
}
