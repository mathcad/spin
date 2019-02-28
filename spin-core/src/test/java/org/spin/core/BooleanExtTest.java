package org.spin.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.spin.core.security.Base64;
import org.spin.core.util.BooleanExt;
import org.spin.core.util.StringUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * <p>Created by xuweinan on 2017/9/4.</p>
 *
 * @author xuweinan
 */
public class BooleanExtTest {
    @Test
    public void of(TestInfo testInfo) {
        Long res = BooleanExt.ofAny(false).yes(() -> 1L).otherwise(() -> -1L);
        assertTrue(res.equals(-1L));
        res = BooleanExt.ofAny(true).yes(() -> 1L).otherwise(() -> -1L);
        assertTrue(res.equals(1L));
        res = BooleanExt.ofAny(true).no(() -> 1L).otherwise(() -> -1L);
        assertTrue(res.equals(-1L));
        res = BooleanExt.ofAny(false).no(() -> 1L).otherwise(() -> -1L);
        assertTrue(res.equals(1L));

        BooleanExt.of(true).yes(() -> System.out.println("true")).otherwise(() -> System.out.println("false"));
    }

    @Test
    public void testB() {
        System.out.println("Basic " + Base64.encode(StringUtils.getBytesUtf8("omall:123456")));


        System.out.println(StringUtils.newStringUtf8(Base64.decode("bWFsbDoxMjM0NTY=")));
        System.out.println(StringUtils.newStringUtf8(Base64.decode("ZWxhc3RpYzpCb25hZGUjZWxhc3RpYw==")));
    }

}
