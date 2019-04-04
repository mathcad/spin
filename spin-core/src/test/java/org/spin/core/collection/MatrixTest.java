package org.spin.core.collection;

import org.junit.jupiter.api.Test;
import org.spin.core.auth.KeyInfo;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by Arvin on 2017/3/7.
 */
public class MatrixTest {

    @Test
    public void testInsert() {
        assertTrue(true);
    }

    @Test
    public void tesetSet() {
        KeyInfo keyInfo1 = new KeyInfo("1", "123", "aaa", "defautl", 0L);
        KeyInfo keyInfo2 = new KeyInfo("2", "456", "bbb", "defautl", 0L);
        KeyInfo keyInfo3 = new KeyInfo("2", "456", "bbb", "defautl", 0L);
        Set<KeyInfo> keys = new HashSet<>();
        keys.add(keyInfo1);
//        keys.add(keyInfo2);
        keys.add(keyInfo3);

        KeyInfo keyInfo4 = keys.stream().filter(k -> k.getIdentifier().equals("2")).findFirst().orElse(null);
//        keyInfo4.updateKey("234","ccc", 0L);
        keys.add(keyInfo2);
        assertTrue(true);
    }

    @Test
    public void testMatrix() {
        Matrix<String> matrix = new Matrix<>(3, 0, 1, 2);

        matrix.setHeader(0, "id");
        matrix.setHeader(1, "key");
        matrix.setHeader(2, "token");

        matrix.insert("1", "key1", "token1");
        matrix.insert("2", "key2", "token2");
        matrix.insert("3", "key3", "token3");
        matrix.insert("4", "key4", "token4");
        matrix.insert("5", "key5", "token5");
        matrix.insert("6", "key6", "token6");
        matrix.insert("7", "key7", "token7");
        matrix.insert("8", "key8", "token8");
        matrix.insert("9", "key9", "token9");
        matrix.insert("10", "key10", "token10");

        Row<String> row3 = matrix.findRows("token", "token3").get(0);

        row3.set(0, "2");

        List<Row<String>> rows = matrix.findRows("id", "2");

        matrix.findRows("id", "3");

        matrix.update("token", "token3", "3", "key3", "token3");

        matrix.findRows("id", "3");

        row3.delete();

        matrix.findRows("token", "token3");
    }

    public static void main(String[] args) {
        String[] src = {"XXXL","M", "s", "xxxs","xs", "xl"};

        Map<String, List<String>> collect = Arrays.stream(src).map(String::toUpperCase).collect(Collectors.groupingBy(i -> i.substring(i.length() - 1)));

        List<String> res = new ArrayList<>(src.length);
        collect.get("S").stream().sorted((a, b) -> b.length() - a.length()).forEach(res::add);
        collect.get("M").stream().sorted(Comparator.comparingInt(String::length)).forEach(res::add);
        collect.get("L").stream().sorted(Comparator.comparingInt(String::length)).forEach(res::add);

        System.out.println(JsonUtils.toJson(res));
    }
}
