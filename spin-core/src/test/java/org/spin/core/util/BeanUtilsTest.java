package org.spin.core.util;

import org.junit.jupiter.api.Test;
import org.spin.core.session.SimpleSession;

import java.time.LocalDateTime;
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
        Integer no = BeanUtils.getFieldValue(map, "parent.address[0].no");
        long e = System.currentTimeMillis();
        System.out.println(e - s);
        assertEquals(255, no);
        s = System.currentTimeMillis();
        no = BeanUtils.getFieldValue(map, "parent.address.elementData[1].no");
        e = System.currentTimeMillis();
        System.out.println(e - s);
        assertEquals(5, no);
        Class size = BeanUtils.getFieldValue(map, "parent.#class");
        System.out.println(size.getName());
    }

    @Test
    public void testToMap() {
        SimpleSession simpleSession = new SimpleSession();
        simpleSession.setAttribute("aaa", new SimpleSession());
        Map<String, Object> stringObjectMap;
        long s = System.currentTimeMillis();
        stringObjectMap = BeanUtils.toMap(simpleSession, true);
        long e = System.currentTimeMillis();
        System.out.println(e - s);
        System.out.println(JsonUtils.toJson(stringObjectMap));
    }

    @Test
    public void testCopy() {
        Ba a = new Ba();
        a.setName("xxx");
        a.setAddress("asdfasdfasdfasdf");
        a.setNick("nick");
        a.setAge(10);

        Bb b = new Bb();
//        BeanUtils.copyTo(a, b, Ba::getName, Ba::getName, Ba::getAge);


        BeanUtils.copyTo(a, b, Ba::getName, Ba::getAddress);
        assertEquals(b.name, "xxx");
        assertEquals(b.address, "asdfasdfasdfasdf");
        b = new Bb();
        BeanUtils.copyTo(a, b);
        assertEquals(b.name, "xxx");
        assertEquals(b.address, "asdfasdfasdfasdf");

    }

    @Test
    public void testLambda() {
        DemoA a = new DemoA();
        a.setAge(100L);
        a.setName("asdf");
        a.setNickName("镜");
        a.setCreateTime(LocalDateTime.now());

        DemoB b = new DemoB();
        BeanUtils.copyTo(a, b, a::getName, a::getAge, a::getCreateTime, a::getNickName);

        System.out.println(b.getAge());

        b = new DemoB();
        b.setName("123");
        BeanUtils.copyToIgnore(a, b, a::getName);
        System.out.println(b);
    }

    public static class Ba {
        public String name;
        private String address;
        private int age;
        private String nick;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getNick() {
            return nick;
        }

        public void setNick(String nick) {
            this.nick = nick;
        }
    }

    public static class Bb {
        private String name;
        private String address;
        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}


class DemoA {
    private String name;
    private String nickName;
    private LocalDateTime createTime;
    private long age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }
}

class DemoB {
    private String name;
    private String nickName;
    private LocalDateTime createTime;
    private Double age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Double getAge() {
        return age;
    }

    public void setAge(Double age) {
        this.age = age;
    }
}
