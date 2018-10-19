package org.spin.enhance.pinyin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 拼音方案映射
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/10/15</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class PinyinMapping {

    private static Map<String, Map<String, GwoyeuMappingItem>> gwoyeuItems;

    private static Map<String, Map<String, MappingItem>> items;

    static {
        gwoyeuItems = new HashMap<>(8);
        gwoyeuItems.put("hanyu", new HashMap<>(512));
        gwoyeuItems.put("gwoyeuI", new HashMap<>(512));
        gwoyeuItems.put("gwoyeuIi", new HashMap<>(512));
        gwoyeuItems.put("gwoyeuIii", new HashMap<>(512));
        gwoyeuItems.put("gwoyeuIv", new HashMap<>(512));
        gwoyeuItems.put("gwoyeuV", new HashMap<>(512));

        items = new HashMap<>(8);
        items.put("hanyu", new HashMap<>(512));
        items.put("wade", new HashMap<>(512));
        items.put("mpsii", new HashMap<>(512));
        items.put("yale", new HashMap<>(512));
        items.put("tongyong", new HashMap<>(512));

        try (BufferedReader bf = new BufferedReader(new InputStreamReader(PinyinMapping.class.getClassLoader().getResourceAsStream("pinyindb/pinyin_gwoyeu_mapping")))) {
            String temp;
            GwoyeuMappingItem gwoyeuMappingItem = new GwoyeuMappingItem();
            while ((temp = bf.readLine()) != null) {
                if (temp.length() == 0) {
                    gwoyeuItems.get("hanyu").put(gwoyeuMappingItem.getHanyu(), gwoyeuMappingItem);
                    gwoyeuItems.get("gwoyeuI").put(gwoyeuMappingItem.getGwoyeuI(), gwoyeuMappingItem);
                    gwoyeuItems.get("gwoyeuIi").put(gwoyeuMappingItem.getGwoyeuIi(), gwoyeuMappingItem);
                    gwoyeuItems.get("gwoyeuIii").put(gwoyeuMappingItem.getGwoyeuIii(), gwoyeuMappingItem);
                    gwoyeuItems.get("gwoyeuIv").put(gwoyeuMappingItem.getGwoyeuIv(), gwoyeuMappingItem);
                    gwoyeuItems.get("gwoyeuV").put(gwoyeuMappingItem.getGwoyeuV(), gwoyeuMappingItem);
                    gwoyeuMappingItem = new GwoyeuMappingItem();
                } else {
                    String[] split = temp.split(":");
                    gwoyeuMappingItem.setValue(split[0], split[1]);
                }
            }
        } catch (IOException ignore) {
        }

        try (BufferedReader bf = new BufferedReader(new InputStreamReader(PinyinMapping.class.getClassLoader().getResourceAsStream("pinyindb/pinyin_mapping")))) {
            String temp;
            MappingItem mappingItem = new MappingItem();
            while ((temp = bf.readLine()) != null) {
                if (temp.length() == 0) {
                    items.get("hanyu").put(mappingItem.getHanyu(), mappingItem);
                    items.get("wade").put(mappingItem.getWade(), mappingItem);
                    items.get("mpsii").put(mappingItem.getMpsii(), mappingItem);
                    items.get("yale").put(mappingItem.getYale(), mappingItem);
                    items.get("tongyong").put(mappingItem.getTongyong(), mappingItem);
                    mappingItem = new MappingItem();
                } else {
                    String[] split = temp.split(":");
                    mappingItem.setValue(split[0], split[1]);
                }
            }
        } catch (IOException ignore) {
        }

    }

    public static Map<String, Map<String, GwoyeuMappingItem>> getGwoyeuItems() {
        return gwoyeuItems;
    }

    public static Map<String, Map<String, MappingItem>> getItems() {
        return items;
    }
}
