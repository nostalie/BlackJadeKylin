package org.nostalie.auto.modify;


import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import org.nostalie.auto.mysql.JavaType;
import org.nostalie.auto.pojo.CRUDContext;
import org.nostalie.auto.pojo.ColumnInfo;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.nostalie.auto.mysql.JoinSQL.EMPTY;

/**
 * @author nostalie on 17-8-17.
 */
public class KylinUtils {

    public static final Splitter VERTICAL = Splitter.on("|");

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
        String random = UUID.randomUUID().toString();
        return base + random;
    }

    public static boolean equalsIgnoreCase(String s1, String s2){
        if(s1==null && s2 ==null){
            return true;
        }else if(s1 == null || s2 == null){
            return false;
        }else if(s1.toUpperCase().equals(s2.toUpperCase())){
            return true;
        }
        return false;
    }

    public static String firstUpper(String name){
        Preconditions.checkArgument(name != null && !EMPTY_STRING.equals(name));
        return name.substring(0,1).toUpperCase() + name.substring(1);
    }

    public static String nullToEmpty(String s){
        return s == null? "" : s;
    }

    public static boolean isEmpty(Iterable<?> iterable) {
        return iterable == null || !iterable.iterator().hasNext();
    }

    public static boolean isEmpty(Map<?,?> map){
        return map == null || map.size() == 0;
    }

}
