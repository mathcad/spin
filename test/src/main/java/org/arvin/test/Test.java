package org.arvin.test;

import org.arvin.test.domain.sys.User;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Arvin on 2016/6/20.
 */
public class Test {
    public static void main(String[] args) {
        Date d = new Date(System.currentTimeMillis());
        System.out.println(d.toString());
        Map<String, Object> a = new HashMap<>();
        a.put("id", 1L);
        a.put("name", "arvin");
        a.put("realName", "real");
        a.put("organ.id", 10L);
        a.put("organ.name", "ooo");
        List<Map<String, Object>> r = new ArrayList<>();
        HashMap r1 = new HashMap();
        r1.put("id", 20L);
        r1.put("name", "r1");
        r.add(r1);
        r1 = new HashMap();
        r1.put("id", 21L);
        r1.put("name", "r2");
        r.add(r1);
        a.put("roles", r);
        User u = new User();
        try {
            int i = -1;
            Long start = System.currentTimeMillis();
            while (++i != 100000)
              u = org.spin.util.BeanUtils.wrapperMapToBean(User.class, a);
            Long end = System.currentTimeMillis();

//            System.out.println(u.getRoles());
            System.out.println(end - start);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

//        System.out.println(u.getRoles());
    }
}
