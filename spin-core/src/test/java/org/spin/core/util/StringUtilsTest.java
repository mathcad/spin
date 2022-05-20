package org.spin.core.util;

import org.junit.jupiter.api.Test;
import org.spin.core.stream.SpinCollectors;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringUtilsTest {

    @Test
    public void testJoin() {
        String[] args = new String[]{"a", "b", "c", null, "e"};
        String join = StringUtils.join(args, ",");
        assertEquals(join, "a,b,c,e");
    }

    @Test
    public void testReduce() {
        String[] s = {"a", null, "c", "d", null, "f"};
        String s1 = Arrays.stream(s).filter(Objects::nonNull).reduce((a, b) -> a + "," + b).orElse("");
        System.out.println(s1);
    }

    @Test
    public void testCameUnderscore() {
        String test = "getFirstNameById";
        String underscore = StringUtils.underscore(test);
        assertEquals("get_first_name_by_id", underscore);

        test = StringUtils.camelCase(underscore);
        assertEquals("getFirstNameById", test);


        test = StringUtils.camelCase("get__first_name_by_id__");
        assertEquals("get_FirstNameById__", test);
    }

    @Test
    public void testReverse() {
        String test = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz" +
            "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz" +
            "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz" +
            "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz" +
            "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz" +
            "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz";
        String reverse;
        long s = System.currentTimeMillis();
        reverse = StringUtils.reverse(test);
        long e = System.currentTimeMillis();
        System.out.println(reverse);
        System.out.println(e - s);

        s = System.currentTimeMillis();
        reverse = new StringBuilder(test).reverse().toString();
        e = System.currentTimeMillis();
        System.out.println(reverse);
        System.out.println(e - s);
    }

    @Test
    public void testRender() {
        String tmpl = "aasdfa\\${123sdf${#a}${#b}${#dfsdfsd}";
        System.out.println(StringUtils.render(tmpl, MapUtils.ofMap("a", "--", "b", "++++")));
    }

    @Test
    public void testEncode() {
        String tmp = "aaaaa这是第一段中文bbbbb这是第2段中文。，l";
        System.out.println(StringUtils.urlEncodeChinese(tmp));

        System.out.println(StringUtils.urlDecode(StringUtils.urlEncodeChinese(tmp)));
    }

    @Test
    public void testTrim() {
        String tmp = "   aaa   ";
        System.out.println(StringUtils.trimWhitespace(tmp));
    }

    @Test
    void testUrlEncode() {
        System.out.println(StringUtils.urlEncode("访客管理系统1 1581324513055"));
        System.out.println(StringUtils.urlEncode("     +"));
        System.out.println(StringUtils.urlDecode("%E8%AE%BF%E5%AE%A2%E7%AE%A1%E7%90%86%E7%B3%BB%E7%BB%9F1%201581324513055%2B*-.%20+"));
    }

    @Test
    void testaaa() {
        List<Dto> list = new ArrayList<>();
        Dto dto = new Dto();
        dto.setType(1);
        dto.setIdPath("1,2,3");
        list.add(dto);

        dto = new Dto();
        dto.setType(1);
        dto.setIdPath("1,2,4");
        list.add(dto);

        dto = new Dto();
        dto.setType(2);
        dto.setIdPath("4,5");
        list.add(dto);


        String c = ";";
        List<Vo> result = new LinkedList<>();
        list.stream().collect(SpinCollectors.groupingBy(Dto::getType, i -> StringUtils.splitToSet(i.getIdPath(), c, Long::valueOf),
            SpinCollectors.mergeSet(Vo::new))).forEach((k, v) -> {
            v.type = k;
            result.add(v);
        });

        Map<Integer, Set<String>> collect = list.stream().collect(SpinCollectors.groupingBy(Dto::getType, Dto::getIdPath,
            SpinCollectors.joining(c, i -> StringUtils.splitToSet(i, c))));

        System.out.println(JsonUtils.toJson(collect));
        System.out.println(JsonUtils.toJson(result));
    }


    class Dto {
        int type;
        String idPath;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getIdPath() {
            return idPath;
        }

        public void setIdPath(String idPath) {
            this.idPath = idPath;
        }
    }

    class Vo {
        int type;
        Set<Long> idPath;

        public Vo(Set<Long> idPath) {
            this.idPath = idPath;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public Set<Long> getIdPath() {
            return idPath;
        }

        public void setIdPath(Set<Long> idPath) {
            this.idPath = idPath;
        }
    }

    @Test
    void testId() {
        String idCard = "340207199001170015";
        System.out.println(IdCardUtils.isValid(idCard));
        System.out.println(IdCardUtils.getBirthDate(idCard));
        System.out.println(IdCardUtils.getGenderByIdCard(idCard));
        System.out.println(IdCardUtils.getAgeByIdCard(idCard));
    }

    @Test
    void testRandom() {
        Set<String> a = new HashSet<>(256);
        for (int i = 0; i < 128; i++) {
            a.add(RandomStringUtils.randomAlphabetic(16));
        }

        System.out.println(a.size());
        System.out.println(a);
    }

}
