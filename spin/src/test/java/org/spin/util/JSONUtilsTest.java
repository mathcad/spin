package org.spin.util;

import org.junit.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.Assert.assertTrue;

/**
 * Created by Arvin on 2017/1/25.
 */
public class JSONUtilsTest {

    @Test
    public void testLocalDateTime() {
        LocalDateTime dateTime = LocalDateTime.now();
        System.out.println(JSONUtils.toJson(dateTime));
        Timestamp timestamp = Timestamp.valueOf(dateTime);
        System.out.println(JSONUtils.toJson(timestamp));
        assertTrue(true);
    }

}