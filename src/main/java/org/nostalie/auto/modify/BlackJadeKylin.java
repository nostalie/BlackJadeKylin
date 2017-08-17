package org.nostalie.auto.modify;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * Created by nostalie on 17-8-15.
 */
public class BlackJadeKylin {

    public static void main(String[] args) throws NotFoundException, CannotCompileException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        ClassPool pool = ClassPool.getDefault();
        CtClass cc = pool.makeClass("org.nostalie.auto.pojo.Test");

        CtField cf = new CtField(pool.get("java.lang.String"),"name",cc);
        cf.setModifiers(Modifier.PRIVATE);

        cc.addMethod(CtNewMethod.setter("setName",cf));
        cc.addMethod((CtNewMethod.getter("getName",cf)));

        cc.addField(cf, CtField.Initializer.constant(""));

        Object result = cc.toClass().newInstance();

        Method set = result.getClass().getMethod("setName", String.class);
        set.invoke(result,"nostalie");
        Method get = result.getClass().getMethod("getName");
        String name = (String) get.invoke(result);

        System.out.println(name);
    }
}
