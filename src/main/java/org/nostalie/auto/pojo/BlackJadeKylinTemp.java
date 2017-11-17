package org.nostalie.auto.pojo;

import javassist.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.nostalie.auto.modify.BlackJadeKylin;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * @author nostalie on 17-8-15.
 */
public class BlackJadeKylinTemp {
    public static void main(String[] args) throws NotFoundException, CannotCompileException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, IOException {
//        ClassPool pool = ClassPool.getDefault();
//        CtClass cc = pool.makeClass("org.nostalie.auto.pojo.Test");
//
//        CtField cf = new CtField(pool.get("java.lang.String"), "name", cc);
//        cf.setModifiers(Modifier.PRIVATE);
//
//        cc.addMethod(CtNewMethod.setter("setName", cf));
//        cc.addMethod((CtNewMethod.getter("getName", cf)));
//
//        cc.addField(cf, CtField.Initializer.constant(""));
//
//        Object result = cc.toClass().newInstance();
//
//        Method set = result.getClass().getMethod("setName", String.class);
//        set.invoke(result, "nostalie");
//        Method get = result.getClass().getMethod("getName");
//        String name = (String) get.invoke(result);
//
//        System.out.println(name);

        Date date = new Date();
        final BlackJadeKylin kylin = BlackJadeKylin.builder().setField("date",Date.class).build();
        kylin.set("date",date,Date.class);
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(kylin));
    }
}
