package org.spin.common.util;

import org.spin.common.annotation.UtilClass;
import org.spin.core.gson.annotation.PreventOverflow;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.JsonUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据字典上下文
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/2/17</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@UtilClass
public class DataDictionary {
    private static final String ROOT_DICT_REDIS_KEY = "ALL_DATA_DICTIONARY";
    private static final DefaultRedisScript<java.util.List> redisScript = new DefaultRedisScript<java.util.List>();

    private static org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    static {
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("luascript/getDictChildren.lua")));
        redisScript.setResultType(java.util.List.class);
    }

    public static void init(org.springframework.data.redis.core.StringRedisTemplate redisTemplate) {
        DataDictionary.redisTemplate = redisTemplate;
    }

    /**
     * 查询数据字典详情
     *
     * @param code 编码
     * @return 数据字典对象
     */
    public static DictContent findByCode(String code) {
        String content = redisTemplate.<String, String>opsForHash().get(ROOT_DICT_REDIS_KEY, code);
        if (org.spin.core.util.StringUtils.isNotEmpty(content)) {
            return JsonUtils.fromJson(content, DictContent.class);
        }
        return null;
    }

    /**
     * 批量查询数据字典详情
     *
     * @param code 编码集合
     * @return 数据字典对象集合
     */
    public static java.util.List<DictContent> findByCode(java.util.Set<String> code) {
        java.util.List<String> contentList = redisTemplate.<String, String>opsForHash().multiGet(ROOT_DICT_REDIS_KEY, code);
        if (CollectionUtils.isEmpty(code)) {
            return Collections.emptyList();
        }
        return contentList.stream().filter(org.spin.core.util.StringUtils::isNotEmpty).map(c -> JsonUtils.fromJson(c, DictContent.class)).collect(Collectors.toList());
    }

    /**
     * 查询指定父节点下的所有数据字典项
     *
     * @param parentCode 父级编码
     * @return 数据字典项列表
     */
    public static List<DictContent> findByParent(String parentCode) {
        List<?> children = redisTemplate.execute(redisScript, Collections.singletonList(ROOT_DICT_REDIS_KEY), parentCode);
        if (children != null) {
            return children.stream().map(Object::toString).map(it -> JsonUtils.fromJson(it, DictContent.class)).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public static class DictContent {

        /**
         * ID
         */
        @PreventOverflow
        private Long id;

        /**
         * 名称
         */
        private String name;

        /**
         * 编码
         */
        private String code;

        /**
         * 值
         */
        private String value;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
