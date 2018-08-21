package org.spin.core.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/8/17</p>
 *
 * @author xuweinan
 * @version 1.0
 */
class MapUtilsTest {

    @Test
    public void testMapBuild() {
        HashMap<String, String> a = MapUtils.with(HashMap<String, String>::new).ofMap("a", "1");
        System.out.println(a);
        assertTrue(a.containsKey(a));
    }
}
