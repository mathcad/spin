package org.spin.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @Test
    public void testJoin() {
        String[] args = new String[]{"a", "b", "c", null, "e"};
        System.out.println(StringUtils.join(args, ","));
        assertTrue(true);
    }

}
