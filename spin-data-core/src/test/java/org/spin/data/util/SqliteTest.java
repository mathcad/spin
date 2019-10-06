package org.spin.data.util;

import org.junit.jupiter.api.Test;
import org.spin.data.rs.MapRowMapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/10/6</p>
 *
 * @author xuweinan
 * @version 1.0
 */
class SqliteTest {

    @Test
    void testMemory() {
        Map<String, Object> stringObjectMap = Sqlite.inMemoryMode().queryFirst("select 1 as cnt", null, new MapRowMapper());
        System.out.println(stringObjectMap);
        assertEquals(stringObjectMap.get("cnt"), 1);
    }
}
