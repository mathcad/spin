package org.infrastructure.jpa.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用查询参数
 */
public class QueryParam implements Serializable {
    private static final long serialVersionUID = 4033669191222887528L;

    /**
     * 查询的实体
     */
    public String cls;

    /**
     * 查询字段列表
     */
    public List<String> fields = new ArrayList<>();

    /**
     * {name__like:"高某",organ__name__like:"总部"}
     * <p>
     * in 条件请使用,分割的value组合
     * {person__city__in:"wuhu,nanjing"}
     */
    public Map<String, String> q = new LinkedHashMap<>();

    /**
     * 多个排序字段用,隔开
     * name__desc,id__desc
     */
    public String sort;

    public int start = 0;

    public int limit = 50;

    public String getOrderSql() {
        return sort.length() > 0 ? "ORDER BY" + sort.replaceAll("__", " ") : "";
    }
}