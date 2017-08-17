package org.nostalie.auto.modify;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *
 * Created by nostalie on 17-8-15.
 */
public class BlackJadeKylin {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlackJadeKylin.class);
    private static final String SET = "set";
    private static final String GET = "get";

    private static final ClassPool DEFAULT_POOL = ClassPool.getDefault();
    private Object data;
    private List<String> names = Lists.newArrayList();

    private BlackJadeKylin(Builder builder){
        this.data = builder.data;
        this.names = builder.names;
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {

        private CtClass ctClass;
        private Object data;
        private List<String> names = Lists.newArrayList();

        private Builder() {
            try {
                String name = KylinUtils.getRootPath() + KylinUtils.getUniqName();
                ctClass = DEFAULT_POOL.makeClass(name);
            } catch (Exception e) {
                LOGGER.error("获取实例失败", e);
                throw new RuntimeException("获取实例失败", e);
            }
        }

        public Builder setField(Class<?> clazz, String name) throws NotFoundException, CannotCompileException {
            Preconditions.checkNotNull(clazz);
            Preconditions.checkNotNull(name);

            CtField ctField = new CtField(DEFAULT_POOL.get(clazz.getName()), name,ctClass);
            ctField.setModifiers(Modifier.PRIVATE);

            ctClass.addMethod(CtNewMethod.setter(SET + KylinUtils.firstUppper(name),ctField));
            ctClass.addMethod(CtNewMethod.getter(GET + KylinUtils.firstUppper(name),ctField));
            ctClass.addField(ctField);
            this.names.add(name);
            return this;
        }

        public BlackJadeKylin build() throws CannotCompileException, IllegalAccessException, InstantiationException {
            data = ctClass.toClass().newInstance();
            return new BlackJadeKylin(this);
        }
    }



}
