package org.spin.core.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StringUtilsTest {

    @Test
    public void testJoin() {
        String[] args = new String[]{"a", "b", "c", null, "e"};
        System.out.println(StringUtils.join(args, ","));
        assertTrue(true);
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
        System.out.println(underscore);
        test = StringUtils.camelCase(underscore);
        System.out.println(test);

        test = StringUtils.camelCase("get__first_name_by_id__");
        System.out.println(test);
    }

}
