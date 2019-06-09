package org.spin.sys;

import org.hibernate.internal.SessionImpl;
import org.junit.jupiter.api.Test;
import org.spin.core.SpinContext;
import org.spin.core.gson.Gson;
import org.spin.core.gson.reflect.TypeToken;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.ReflectionUtils;
import org.spin.data.query.QueryParam;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Arvin on 2016/6/26.
 */
public class EnumUtilsTest {
    List<Map<String, String>> a = new ArrayList<>();

    @Test
    public void getEnum() throws Exception {
        String t = "asdf|def";
        String[] a = t.split("\\|");
//        rx.Observable.from(a).forEach(new Action1<String>() {
//            @Override
//            public void call(String s) {
//                System.out.println(s);
//            }
//        });
    }

    @Test
    public void getEnum1() throws Exception {
        this.test();
    }

    public void test(String... args) {
        for (String s : args) {
            System.out.println(s);
        }
    }

    @Test
    public void testGeneric() throws NoSuchFieldException {
        Type GTYPE_LIST_MAP = new TypeToken<List<Map<String, String>>>() {
        }.getType();

        System.out.println(Arrays.toString(((ParameterizedType) a.getClass().getGenericSuperclass()).getActualTypeArguments()));
        System.out.println(GTYPE_LIST_MAP);

        QueryParam q = QueryParam.from("java.lang.String");
        q.addField("field");
        q.where("a","a").where("b","c").where("b","c");
        q.desc("id");
        Gson gson = new Gson();
        String str = gson.toJson(q);
        System.out.println(str);
        SpinContext.DEV_MODE = true;
        QueryParam t = QueryParam.parseFromJson(str);
        System.out.println(t.getCls());
    }

    @Test
    public void testRelection() {
        Class c = SessionImpl.class.getSuperclass().getSuperclass();
        try {

            long s = System.currentTimeMillis();
        Field jdbcCoordinator = ReflectionUtils.findField(c, "jdbcCoordinator");
            long e = System.currentTimeMillis();
            System.out.println(e - s);
        System.out.println(jdbcCoordinator.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
