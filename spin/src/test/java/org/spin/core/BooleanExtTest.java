package org.spin.core;

import org.junit.Test;
import org.spin.core.util.BooleanExt;

import static org.junit.Assert.assertTrue;

/**
 * <p>Created by xuweinan on 2017/9/4.</p>
 *
 * @author xuweinan
 */
public class BooleanExtTest {
    @Test
    public void of() throws Exception {
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
