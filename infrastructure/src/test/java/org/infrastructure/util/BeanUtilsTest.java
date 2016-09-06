package org.infrastructure.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.infrastructure.jpa.core.GenericUser;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Created by Arvin on 2016/8/19.
 */
public class BeanUtilsTest {
    @Test
    public void wrapperMapToBean() throws Exception {

        Map<String, Object> values = new HashMap<>();
        values.put("id", 1);
        values.put("realName", "xuweinan");
        values.put("createUser.id", 2);
        values.put("createUser.realName", "admin");
        values.put("createUser.lastUpdateUserName", "xxxxx");
        values.put("createUser.createUser.id", 3);
        values.put("createUser.createUser.realName", "kilee");
        GenericUser user = null;
//        values = BeanUtils.wrapperFlatMap(values);

        int SCALE = 1000000;
        //
        long startTime = System.currentTimeMillis();
        for (int i = 0; i != SCALE; ++i) {
            user = BeanUtils.wrapperMapToBean(GenericUser.class, values);
        }
        long endTime = System.currentTimeMillis();

        float seconds = (endTime - startTime) / 1000F;
        System.out.println("非递归：" + Float.toString(seconds) + " seconds.");

        // 自己实现的递归转换方法
        // 序列化10W次，3.984s
//        GenericUser user2 = null;
//        startTime = System.currentTimeMillis();
//        for (int i = 0; i != SCALE; ++i) {
//            BeanUtils.wrapperMapToBean(GenericUser.class, values, "");
//        }
//        endTime = System.currentTimeMillis();
//
//        seconds = (endTime - startTime) / 1000F;
//        System.out.println("递  归：" + Float.toString(seconds) + " seconds.");


        // json方式反序列化
        // 序列化10W次，0.423s
        GenericUser user3 = null;
        Gson gson = new GsonBuilder().create();
        values.clear();
        values.put("id", 1);
        values.put("realName", "xuweinan");
        Map<String, Object> tmp = new HashMap<>();
        tmp.put("id", 2);
        tmp.put("realName", "admin");
        tmp.put("lastUpdateUserName", "xxxxx");
        Map<String, Object> tmp1 = new HashMap<>();
        tmp1.put("id", 3);
        tmp1.put("realName", "kilee");
        tmp.put("createUser", tmp1);
        values.put("createUser", tmp);

        startTime = System.currentTimeMillis();
        for (int i = 0; i != SCALE; ++i) {
            String json = gson.toJson(values);
            user3 = gson.fromJson(json, GenericUser.class);
        }
        endTime = System.currentTimeMillis();

        seconds = (endTime - startTime) / 1000F;
        System.out.println("JSON：" + Float.toString(seconds) + " seconds.");
        assertTrue(user.getCreateUser().getCreateUser().getId() == 3);
    }

    @Test
    public void testPackageUtil() {
        List<String> res = PackageUtils.getClassName("org");
        assertTrue(null != res && res.size() > 0);
    }
}