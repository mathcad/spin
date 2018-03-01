package org.spin.data.query;

import org.spin.core.util.DigestUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 查询参数谓词部分
 * <p>Created by xuweinan on 2016/9/25.</p>
 *
 * @author xuweinan
 */
public class QueryPredicate implements Serializable {
    private static final long serialVersionUID = 1381761812232116307L;
    private static final String SALT = "54b4ad84eddb38";

    private Map<String, String> conditions = new HashMap<>();
    private String sort = null;

    /**
     * 计算当前查询谓词的签名
     */
    public String calcSignature() {
        StringBuilder msg = new StringBuilder();
        conditions.entrySet().stream().map(entry -> entry.getKey() + entry.getValue()).sorted().forEach(msg::append);
        msg.append(SALT);
        return DigestUtils.md5Hex(msg.toString());
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
     * name desc,id desc
     */
    public String getSort() {
        return sort;
    }

    /**
     * 多个排序字段用,隔开
     * name desc,id desc
     */
    public void setSort(String sort) {
        this.sort = sort;
    }
}
