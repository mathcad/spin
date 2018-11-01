package org.spin.core.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringUtilsTest {

    @Test
    public void testJoin() {
        String[] args = new String[]{"a", "b", "c", null, "e"};
        String join = StringUtils.join(args, ",");
        assertEquals(join, "a,b,c,e");
    }

    @Test
    public void testReduce() {
        String[] s = {"a", null, "c", "d", null, "f"};
        String s1 = Arrays.stream(s).filter(Objects::nonNull).reduce((a, b) -> a + "," + b).orElse("");
        System.out.println(s1);
    }

    @Test
    public void testCameUnderscore() {
        String test = "getFirstNameById";
        String underscore = StringUtils.underscore(test);
        assertEquals("get_first_name_by_id", underscore);

        test = StringUtils.camelCase(underscore);
        assertEquals("getFirstNameById", test);


        test = StringUtils.camelCase("get__first_name_by_id__");
        assertEquals("get_FirstNameById__", test);
    }

    @Test
    public void testReverse() {
        String test = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz" +
            "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz" +
            "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz" +
            "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz" +
            "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz" +
            "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz";
        String reverse;
        long s = System.currentTimeMillis();
        reverse = StringUtils.reverse(test);
        long e = System.currentTimeMillis();
        System.out.println(reverse);
        System.out.println(e - s);

        s = System.currentTimeMillis();
        reverse = new StringBuilder(test).reverse().toString();
        e = System.currentTimeMillis();
        System.out.println(reverse);
        System.out.println(e - s);
    }

    @Test
    public void testRender() {
        String tmpl = "aasdfa\\${123sdf${a}${b}${dfsdfsd}";
        System.out.println(StringUtils.renderParameterMap(tmpl, MapUtils.ofMap("a", "--", "b", "++++")));
    }
}
