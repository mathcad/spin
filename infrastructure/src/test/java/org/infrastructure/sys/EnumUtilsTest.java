package org.infrastructure.sys;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.infrastructure.jpa.api.QueryParam;
import org.junit.Test;

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

        QueryParam q = new QueryParam();
        q.setCls("java.lang.String");
        q.getConditions().put("a", "a");
        q.getConditions().put("b", "b");
        q.getConditions().put("c", "c");
        q.getFields().add("field");
        q.getPredicate().setSort("id__desc");
        Gson gson = new Gson();
        String str = gson.toJson(q);
        System.out.println(str);
        QueryParam t = gson.fromJson(str, QueryParam.class);
        System.out.println(t.getCls());
    }

}