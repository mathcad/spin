package org.spin.core.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <p>Created by xuweinan on 2017/11/23.</p>
 *
 * @author xuweinan
 */
public class NumericUtilsTest {

    @Test
    public void valueCompare(){
        int diff = NumericUtils.valueCompare(0.2, new BigDecimal("0.1").add(new BigDecimal("0.1")));
        System.out.println(diff);
        assertTrue(diff == 0);
        diff = NumericUtils.valueCompare(null, -100);
        System.out.println(diff);
        assertTrue(diff == -1);
        diff = NumericUtils.valueCompare(-10000, null);
        System.out.println(diff);
        assertTrue(diff == 1);
    }

    @Test
    public void testS(){
        assertTrue(StringUtils.isNumeric("123"));
        assertFalse(StringUtils.isNumeric("123d"));
        assertTrue(StringUtils.isNumeric("123.1"));
        assertFalse(StringUtils.isNumeric("123.1d"));
        assertTrue(StringUtils.isNumeric("123.1234"));
        assertFalse(StringUtils.isNumeric("123.12.12"));
        assertFalse(StringUtils.isNumeric("123..1212"));
        assertFalse(StringUtils.isNumeric(".1231212"));
        assertFalse(StringUtils.isNumeric("1231212."));
    }
}
