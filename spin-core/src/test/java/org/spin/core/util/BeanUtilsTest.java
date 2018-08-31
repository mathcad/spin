package org.spin.core.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/8/31</p>
 *
 * @author xuweinan
 * @version 1.0
 */
class BeanUtilsTest {

    @Test
    void getFieldValue() {
        List<Map<String, Object>> maps = CollectionUtils.ofArrayList(MapUtils.ofMap("detail", "aaaaa", "no", 255),
            MapUtils.ofMap("detail", "bbbb", "no", 5));
        Map<String, Object> map = MapUtils.ofMap("id", 1, "parent", MapUtils.ofMap("name", "Zoe", "address", maps));
        long s = System.currentTimeMillis();
        int no = BeanUtils.getFieldValue(map, "#parent.#address.#0.#no");
        long e = System.currentTimeMillis();
        System.out.println(e - s);
        assertEquals(255, no);
        s = System.currentTimeMillis();
        no = BeanUtils.getFieldValue(map, "#parent.#address.elementData.#1.#no");
        e = System.currentTimeMillis();
        System.out.println(e - s);
        assertEquals(5, no);
    }
}
