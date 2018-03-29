package org.spin.core.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

public class JsonUtilsTest {
    @Test
    public void testJson() {
        String a = "{date: '2016-08-31'}";
        String b = "2016-08-31";
        System.out.println(JsonUtils.fromJson(a, A.class).date);
        System.out.println(JsonUtils.fromJson(b, LocalDate.class).toString());
    }
    public static class A {
        LocalDate date;
    }
}
