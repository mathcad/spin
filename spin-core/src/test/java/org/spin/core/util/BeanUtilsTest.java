package org.spin.core.util;

import org.junit.jupiter.api.Test;

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
        Map<String, Object>[] maps = CollectionUtils.ofArray(MapUtils.ofMap("detail", "aaaaa", "no", 255),
            MapUtils.ofMap("detail", "bbbb", "no", 5));
        Map<String, Object> map = MapUtils.ofMap("id", 1, "parent", MapUtils.ofMap("name", "Zoe", "address", maps));
        int no = BeanUtils.getFieldValue(map, "#parent.#address.#0.#no");
        assertEquals(255, no);
        no = BeanUtils.getFieldValue(map, "#parent.#address.#1.#no");
        assertEquals(5, no);
    }
}
