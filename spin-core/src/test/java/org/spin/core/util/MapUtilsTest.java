package org.spin.core.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
//        Map<String, String> a = MapUtils.of(HashMap<String, String>::new).with("a", "1").get();
        Map<String, String> a = MapUtils.ofStringHashMap().with("a", "1", "b", "2").get();
        System.out.println(a);
        assertTrue(a.containsKey("a"));
    }
}
