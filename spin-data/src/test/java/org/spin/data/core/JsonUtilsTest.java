package org.spin.data.core;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import org.spin.core.TypeIdentifier;
import org.spin.core.util.JsonUtils;
import org.spin.enhance.gson.annotation.DatePattern;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by Arvin on 2017/1/25.
 */
public class JsonUtilsTest {

    @Test
    public void testLocalDateTime() {
        LocalDateTime dateTime = LocalDateTime.now();
        System.out.println(JsonUtils.toJson(dateTime));
        Timestamp timestamp = Timestamp.valueOf(dateTime);
        System.out.println(JsonUtils.toJson(timestamp));
        assertTrue(true);
    }

    @Test
    public void testParse() {
        List<Map<String, Object>> r = JsonUtils.fromJson("[{\"id\":\"53632405-7d24-4cc3-8399-b767bb6d7ac3\",\"name\":\"预付网银\",\"charge\":400},{\"id\":\"1\",\"name\":\"押金(现金)\",\"charge\":0},{\"id\":\"d2501c55-578e-4f87-b393-ea99ac08dfdf\",\"name\":\"预付油卡\",\"charge\":300},{\"id\":\"e4f0a938-f057-4b1d-93fc-de16286e3284\",\"name\":\"预付现金\",\"charge\":0},{\"id\":\"b1103190-b8d9-465c-b3cf-36ef92c84ae5\",\"name\":\"结付网银\",\"charge\":0},{\"id\":\"b76b1aac-a4a1-4a8f-b78e-56a4bb4ed91b\",\"name\":\"结付油卡\",\"charge\":900}]", new TypeIdentifier<List<Map<String, Object>>>() {
        });

        System.out.println(r);
    }

    @Test
    public void testEntityId() {
        E a = new E();
        a.setId(81241321817279489L);
        a.setCreateUserId(9007299254740992L);
        a.setXxx(LocalDateTime.now());
        a.setUpdateUserId(2L);
        System.out.println(JsonUtils.toJson(a));
        String b = "{\"id\":81241321817279489,\"createUserId\":'9007299254740992',\"updateUserId\":2,\"version\":0,\"orderNo\":0.0,\"valid\":true,xxx:'2018031212'}";
        AbstractEntity c = JsonUtils.fromJson(b, E.class);
        System.out.println(c);
    }

    @Test
    public void testEnum() {
        String json = "{id: 81241321817279489, status:1, type:2}";
        E e = JsonUtils.fromJson(json, E.class);
        assertTrue(true);
    }

    @Test
    public void testStringFormat() {
        System.out.println(String.format("sqlId: %s%nsqlText: %s", "aaaaa", "bbbbb"));
        assertTrue(true);
    }

    @Test
    public void testBug() {
        String a = "{\"rows\":[{\"id\":107875263349522433,\"name\":\"1\",\"type\":\"教育\",\"status\":5},{\"id\":107875650970320897,\"name\":\"\",\"status\":5},{\"id\":107946270970085377,\"name\":\"共生\",\"enroll_end\":\"三月 8, 2018\",\"actualArea\":100.0,\"actualCost\":600.00,\"type\":\"商业\",\"item\":\"办公\",\"status\":1,\"filePath\":\"/201803/09134517Qg0UJd0P.png\"}],\"total\":3,\"pageSize\":10000000}";
        Page<Map<String, Object>> p = new Gson().fromJson(a, new TypeToken<Page<Map<String, Object>>>(){}.getType());
        System.out.println(JsonUtils.toJson(p));
    }

    @Test
    public void testBug2() {

        Page<Map<String, Object>> p = new Page<>();
        List<Map<String, Object>> r = new ArrayList<>();
        Map<String, Object> d = new HashMap<>();
        d.put("name", "sb");
        d.put("time", LocalDateTime.now());
        r.add(d);
        p.setRows(r);
        System.out.println(JsonUtils.toJson(p));
    }
}

class E extends AbstractEntity {

    @DatePattern(write = "yyyyMMddHH")
    private LocalDateTime xxx;
    private Status status;
    private Type type;

    public LocalDateTime getXxx() {
        return xxx;
    }

    public void setXxx(LocalDateTime xxx) {
        this.xxx = xxx;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}

enum Status implements UserEnumColumn {
    A(1), B(2);

    private int value;
    Status(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }
}

enum Type implements UserEnumColumn {
    C(1), D(2);

    private int value;
    Type(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }
}
