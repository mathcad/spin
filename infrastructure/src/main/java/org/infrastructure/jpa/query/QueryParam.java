package org.infrastructure.jpa.query;

import org.infrastructure.sys.EnvCache;
import org.infrastructure.sys.ErrorAndExceptionCode;
import org.infrastructure.sys.TypeIdentifier;
import org.infrastructure.throwable.SimplifiedException;
import org.infrastructure.util.JSONUtils;
import org.infrastructure.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 通用查询参数
 */
public class QueryParam implements Serializable {
    private static final long serialVersionUID = 4033669191222887528L;
    private static final Logger logger = LoggerFactory.getLogger(QueryParam.class);
    private static final TypeIdentifier<QueryParam> thisType = new TypeIdentifier<QueryParam>() {
    };

    ///页面查询需要携带的信息，其中cls必填////////////////////////////////////////////////////////////////
    private String cls;
    private Set<String> fields = new HashSet<>();
    private Map<String, String> aliasMap = new HashMap<>();
    private QueryPredicate predicate = new QueryPredicate();
    private String signature;
    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static QueryParam parseFromJson(String jsonString) {
        QueryParam qp = JSONUtils.fromJson(jsonString, thisType);
        if (!EnvCache.devMode && !qp.validation())
            throw new SimplifiedException(ErrorAndExceptionCode.SIGNATURE_FAIL, "请求参数被客户端篡改");
        return qp;
    }

    /**
     * 解析order部分
     */
    public String parseOrder(String tableAlias) {
        return StringUtils.isNotEmpty(predicate.getSort()) ? "ORDER BY" + predicate.getSort().replaceAll("__", " ").replaceAll(",\\s*", ", " + tableAlias + ".") : "";
    }

    /**
     * 验证查询条件是否被客户端篡改
     */
    public boolean validation() {
        String sign = predicate.calcSignature();
        if (logger.isDebugEnabled())
            logger.debug("the actual signature is: " + sign);
        return sign.equals(signature);
    }

    ///QueryParam快速构造////////////////////////////////////////////////////////////////////////////

    /**
     * 创建新的查询参数
     */
    public static QueryParam create() {
        return new QueryParam();
    }

    /**
     * 查询的实体类
     */
    public QueryParam from(String entityClass) {
        this.cls = entityClass;
        return this;
    }

    /**
     * 增加查询字段
     */
    public QueryParam addField(String field) {
        this.fields.add(field);
        return this;
    }

    /**
     * 增加查询字段
     */
    public QueryParam addFields(String... fields) {
        Collections.addAll(this.fields, fields);
        return this;
    }

    /**
     * 增加查询字段别名
     */
    public QueryParam createAlias(String field, String alias) {
        this.aliasMap.put(field, alias);
        return this;
    }

    /**
     * 增加查询字段别名
     */
    public QueryParam createAliases(String... params) {
        if (params.length % 2 != 0) {
            throw new IllegalArgumentException("别名映射参数长度必须为偶数");
        }
        for (int i = 0; i < params.length; ) {
            this.aliasMap.put(params[i], params[i + 1]);
            i += 2;
        }
        return this;
    }

    /**
     * 增加查询条件
     */
    public QueryParam where(String condition, String value) {
        predicate.getConditions().put(condition, value);
        return this;
    }

    /**
     * 增加排序(升序)
     */
    public QueryParam asc(String orderField) {
        predicate.setSort((StringUtils.isEmpty(predicate.getSort()) ? "" : predicate.getSort()) + "," + orderField + "__asc");
        return this;
    }

    /**
     * 增加排序(降序)
     */
    public QueryParam desc(String orderField) {
        predicate.setSort((StringUtils.isEmpty(predicate.getSort()) ? "" : predicate.getSort()) + "," + orderField + "__desc");
        return this;
    }

    /**
     * 分页
     */
    public QueryParam page(int pageIdx, int pageSize) {
        predicate.setPageIdx(pageIdx);
        predicate.setPageSize(pageSize);
        return this;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 查询的实体类
     */
    public String getCls() {
        return cls;
    }

    /**
     * 查询的实体类
     */
    public void setCls(String cls) {
        this.cls = cls;
    }

    /**
     * 查询的字段列表
     */
    public Set<String> getFields() {
        return fields;
    }

    /**
     * 查询的字段列表
     */
    public void setFields(Set<String> fields) {
        this.fields = fields;
    }

    /**
     * 查询的字段别名
     */
    public Map<String, String> getAliasMap() {
        return aliasMap;
    }

    /**
     * 查询的字段别名
     */
    public void setAliasMap(Map<String, String> aliasMap) {
        this.aliasMap = aliasMap;
    }

    /**
     * 查询条件
     */
    public QueryPredicate getPredicate() {
        return predicate;
    }

    /**
     * 查询条件
     */
    public void setPredicate(QueryPredicate predicate) {
        this.predicate = predicate;
    }

    /**
     * {name__like:"高某",organ__name__like:"总部"}
     * <p>
     * in 条件请使用,分割的value组合
     * {person__city__in:"wuhu,nanjing"}
     */
    public Map<String, String> getConditions() {
        return predicate.getConditions();
    }

    /**
     * 多个排序字段用,隔开
     * name__desc,id__desc
     */
    public String getSort() {
        return predicate.getSort();
    }

    /**
     * 分页页码，从1开始
     */
    public int getPageIdx() {
        return predicate.getPageIdx();
    }

    /**
     * 分页大小
     */
    public Integer getPageSize() {
        return predicate.getPageSize();
    }
}