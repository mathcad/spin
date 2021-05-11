package org.spin.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.spin.core.concurrent.LockTicket;
import org.spin.core.function.serializable.Supplier;
import org.spin.core.security.Base64;
import org.spin.core.util.BooleanExt;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    @Test
    void testXX() {
        LockTicket ticket = new LockTicket(false, "aaa", "aaaa", null);
        List<Long> otherwise = ticket.<List<Long>>ifSuccess(() -> CollectionUtils.ofLinkedList(1L, 2L)).otherwise(Collections::emptyList);
        System.out.println(otherwise);
        ticket.ifSuccess(() -> System.out.println("aaaa")).otherwise(() -> System.out.println("bbbbb"));
    }

    public static void main(String[] args) {

        int[] s = {41, 6, 25, 14};

        int cnt = 1000000;
        System.out.println(Arrays.toString(split(s)));
        System.out.println(Arrays.toString(split2(s)));
        long a1 = System.currentTimeMillis();
        while (cnt-- != 0) {
            split(s);
        }
        long a2 = System.currentTimeMillis();
        System.out.println(a2 - a1);
        cnt = 1000000;
        a1 = System.currentTimeMillis();
        while (cnt-- != 0) {
            split2(s);
        }
        a2 = System.currentTimeMillis();
        System.out.println(a2 - a1);
    }

    private static int[] split(int... s) {
        int num;
        int[] mark = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int size = 0;
        int pos;

        for (int i : s) {
            num = i < 0 ? -i : i;
            while (num > 10) {
                pos = num % 10;
                if (mark[pos] != 0) {
                    size++;
                }
                mark[num % 10] = 1;
                num = num / 10;
            }
            ++size;
            mark[num] = 1;
        }

        int[] res = new int[size];
        for (int i = 0; i < mark.length; i++) {
            if (mark[i] > 0) {
                res[--size] = i;
            }
        }
        return res;
    }

    private static Object[] split2(int[] s) {
        return StringUtils.join(s, '\0').chars()
            .distinct().sorted().skip(1).mapToObj(a -> String.valueOf((char) a)).toArray();
    }
}
