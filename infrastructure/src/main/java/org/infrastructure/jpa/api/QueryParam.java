package org.infrastructure.jpa.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用查询参数
 */
public class QueryParam implements Serializable {
    private static final long serialVersionUID = 4033669191222887528L;

    private String cls;
    private List<String> fields = new ArrayList<>();
    private Map<String, String> conditions = new HashMap<>();
    private String sort;
    private int start = 0;
    private int limit = 50;

    /**
     * 查询的实体类
     */
    public String getCls() {
        return cls;
    }

    public void setCls(String cls) {
        this.cls = cls;
    }

    /**
     * 查询的字段列表
     */
    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    /**
     * {name__like:"高某",organ__name__like:"总部"}
     * <p>
     * in 条件请使用,分割的value组合
     * {person__city__in:"wuhu,nanjing"}
     */
    public Map<String, String> getConditions() {
        return conditions;
    }

    public void setConditions(Map<String, String> conditions) {
        this.conditions = conditions;
    }

    /**
     * 多个排序字段用,隔开
     * name__desc,id__desc
     */
    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String parseOrder() {
        return sort.length() > 0 ? "ORDER BY" + sort.replaceAll("__", " ") : "";
    }
}