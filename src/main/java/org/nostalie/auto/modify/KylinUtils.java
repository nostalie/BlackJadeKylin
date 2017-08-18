package org.nostalie.auto.modify;


import com.google.common.base.Preconditions;

/**
 * Created by nostalie on 17-8-17.
 */
public class KylinUtils {

    private static final String EMPTY_STRING = "";

    public static String getRootPath(){
        return BlackJadeKylin.class.getResource("/").getPath();
    }

    public static String getPackageName(){
        return getPackageName(BlackJadeKylin.class);
    }

    public static String getPackageName(Class<?> clazz){
        return clazz.getPackage().getName();
    }

    public static String getUniqName(){
        String base = "Nostaile";
        String random;
        synchronized (KylinUtils.class) {
            random = System.currentTimeMillis() + "";
        }
        return base + random;
    }

    public static String firstUpper(String name){
        Preconditions.checkArgument(name != null && !EMPTY_STRING.equals(name));
        return name.substring(0,1).toUpperCase() + name.substring(1);
    }

    public static String nullToEmpty(String s){
        return s == null? "" : s;
    }
}
