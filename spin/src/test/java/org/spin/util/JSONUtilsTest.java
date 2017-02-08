package org.spin.util;

import org.junit.Test;
import org.spin.sys.TypeIdentifier;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    @Test
    public void testParse() {
        List<Map<String, Object>> r = JSONUtils.fromJson("[{\"id\":\"53632405-7d24-4cc3-8399-b767bb6d7ac3\",\"name\":\"预付网银\",\"charge\":400},{\"id\":\"1\",\"name\":\"押金(现金)\",\"charge\":0},{\"id\":\"d2501c55-578e-4f87-b393-ea99ac08dfdf\",\"name\":\"预付油卡\",\"charge\":300},{\"id\":\"e4f0a938-f057-4b1d-93fc-de16286e3284\",\"name\":\"预付现金\",\"charge\":0},{\"id\":\"b1103190-b8d9-465c-b3cf-36ef92c84ae5\",\"name\":\"结付网银\",\"charge\":0},{\"id\":\"b76b1aac-a4a1-4a8f-b78e-56a4bb4ed91b\",\"name\":\"结付油卡\",\"charge\":900}]", new TypeIdentifier<List<Map<String, Object>>>() {
        });

        System.out.println(r);
    }
}