package org.spin.data.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.core.SpinContext;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.StringUtils;
import org.spin.data.core.PageRequest;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 通用查询参数
 * <p>Created by xuweinan on 2016/12/14.</p>
 *
 * @author xuweinan
 */
public class QueryParam implements Serializable {
    private static final long serialVersionUID = 4033669191222887528L;
    private static final Logger logger = LoggerFactory.getLogger(QueryParam.class);

    ///页面查询需要携带的信息，其中cls必填////////////////////////////////////////////////////////////////
    /**
     * 查询的实体类
     */
    private String cls;
    /**
     * 查询的字段列表
     */
    private Set<String> fields = new HashSet<>();
    /**
     * 查询的字段别名
     */
    private Map<String, String> aliasMap = new HashMap<>();
    /**
     * 查询条件
     */
    private QueryPredicate predicate = new QueryPredicate();
    /**
     * 分页参数
     */
    private PageRequest pagger = new PageRequest();
    private String signature;
    //////////////////////////////////////////////////////////////////////////////////////////////////

    private QueryParam() {
    }

    public static QueryParam parseFromJson(String jsonString) {
        QueryParam qp = JsonUtils.fromJson(jsonString, QueryParam.class);
        if (Objects.isNull(qp)) {
            throw new SimplifiedException("查询参数字符串不合法");
        }
        if (StringUtils.isEmpty(qp.cls)) {
            throw new SimplifiedException("查询参数必须指定查询实体类");
        }
        if (!SpinContext.DEV_MODE && !qp.validation())
            throw new SimplifiedException(ErrorCode.SIGNATURE_FAIL, "请求参数被客户端篡改");
        return qp;
    }

    /**
     * 验证查询条件是否被客户端篡改
     *
     * @return 校验结果
     */
    public boolean validation() {
        String sign = predicate.calcSignature();
        if (logger.isDebugEnabled())
            logger.debug("the actual signature is: " + sign);
        return sign.equals(signature);
    }

    ///QueryParam快速构造////////////////////////////////////////////////////////////////////////////

    /**
     * 创建新的查询参数并设置查询的实体类
     *
     * @param entityClass 实体类全名
     * @return {@link QueryParam}
     */
    public static QueryParam from(String entityClass) {
        QueryParam q = new QueryParam();
        q.cls = entityClass;
        return q;
    }

    /**
     * 增加查询字段
     *
     * @param field 字段名
     * @return {@link QueryParam}
     */
    public QueryParam addField(String field) {
        this.fields.add(field);
        return this;
    }

    /**
     * 增加查询字段
     *
     * @param fields 字段名列表
     * @return {@link QueryParam}
     */
    public QueryParam addFields(String... fields) {
        Collections.addAll(this.fields, fields);
        return this;
    }

    /**
     * 设置查询字段别名
     *
     * @param field 字段名
     * @param alias 字段别名
     * @return {@link QueryParam}
     */
    public QueryParam createAlias(String field, String alias) {
        this.aliasMap.put(field, alias);
        return this;
    }

    /**
     * 设置查询字段别名
     *
     * @param params 字段与别名的映射：field1, alias1, field2, alias2...
     * @return {@link QueryParam}
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
     *
     * @param condition 条件
     * @param value     值
     * @return {@link QueryParam}
     */
    public QueryParam where(String condition, String value) {
        predicate.getConditions().put(condition, value);
        return this;
    }

    /**
     * 增加排序(升序)
     *
     * @param orderField 排序字段
     * @return {@link QueryParam}
     */
    public QueryParam asc(String orderField) {
        predicate.setSort((StringUtils.isEmpty(predicate.getSort()) ? "" : (predicate.getSort() + ",")) + orderField + "__asc");
        return this;
    }

    /**
     * 增加排序(降序)
     *
     * @param orderField 排序字段
     * @return {@link QueryParam}
     */
    public QueryParam desc(String orderField) {
        predicate.setSort((StringUtils.isEmpty(predicate.getSort()) ? "" : (predicate.getSort() + ",")) + orderField + "__desc");
        return this;
    }

    /**
     * 分页
     *
     * @param pageIdx  页码，从1开始
     * @param pageSize 页大小
     * @return {@link QueryParam}
     */
    public QueryParam page(int pageIdx, int pageSize) {
        pagger.setCurrent(pageIdx);
        pagger.setSize(pageSize);
        return this;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////

    public String getCls() {
        return cls;
    }

    public void setCls(String cls) {
        this.cls = cls;
    }

    public Set<String> getFields() {
        return fields;
    }

    public void setFields(Set<String> fields) {
        this.fields = fields;
    }

    public Map<String, String> getAliasMap() {
        return aliasMap;
    }


    public void setAliasMap(Map<String, String> aliasMap) {
        this.aliasMap = aliasMap;
    }

    public QueryPredicate getPredicate() {
        return predicate;
    }

    public void setPredicate(QueryPredicate predicate) {
        this.predicate = predicate;
    }

    public PageRequest getPagger() {
        return pagger;
    }

    public void setPagger(PageRequest pagger) {
        this.pagger = pagger;
    }

    /**
     * {name__like:"高某",organ__name__like:"总部"}
     * <p>
     * in 条件请使用,分割的value组合
     * {person__city__in:"wuhu,nanjing"}
     *
     * @return 查询条件
     */
    public Map<String, String> getConditions() {
        return predicate.getConditions();
    }

    /**
     * 多个排序字段用,隔开
     * name__desc,id__desc
     *
     * @return 排序语句
     */
    public String getSort() {
        return predicate.getSort();
    }

    /**
     * 分页页码，从1开始
     *
     * @return 分页页码
     */
    public Integer getPage() {
        return null == pagger ? null : pagger.getCurrent();
    }

    /**
     * 分页大小
     *
     * @return 分页大小
     */
    public Integer getPageSize() {
        return null == pagger ? null : pagger.getSize();
    }

    /**
     * 将查询参数转换为json字符串
     *
     * @return json字符串
     */
    @Override
    public String toString() {
        return JsonUtils.toJson(this);
    }
}
