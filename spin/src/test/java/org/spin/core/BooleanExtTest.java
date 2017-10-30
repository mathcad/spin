package org.spin.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.spin.core.util.BooleanExt;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * <p>Created by xuweinan on 2017/9/4.</p>
 *
 * @author xuweinan
 */
public class BooleanExtTest {
    @Test
    public void of(TestInfo testInfo) throws Exception {
        Long res = BooleanExt.of(false, Long.class).yes(() -> 1L).otherwise(() -> -1L);
        assertTrue(res.equals(-1L));
        res = BooleanExt.of(true, Long.class).yes(() -> 1L).otherwise(() -> -1L);
        assertTrue(res.equals(1L));
        res = BooleanExt.of(true, Long.class).no(() -> 1L).otherwise(() -> -1L);
        assertTrue(res.equals(-1L));
        res = BooleanExt.of(false, Long.class).no(() -> 1L).otherwise(() -> -1L);
        assertTrue(res.equals(1L));

        BooleanExt.of(true).yes(() -> System.out.println("true")).otherwise(() -> System.out.println("false"));
    }

}
