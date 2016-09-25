package org.infrastructure.jpa.api;

import org.infrastructure.sys.EnvCache;
import org.infrastructure.sys.ErrorAndExceptionCode;
import org.infrastructure.sys.TypeIdentifier;
import org.infrastructure.throwable.SimplifiedException;
import org.infrastructure.util.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 通用查询参数
 */
public class QueryParam implements Serializable {
    private static final long serialVersionUID = 4033669191222887528L;
    private static final Logger logger = LoggerFactory.getLogger(QueryParam.class);
    private static final TypeIdentifier<QueryParam> thisType = new TypeIdentifier<QueryParam>() {
    };

    private String cls;
    private List<String> fields = new ArrayList<>();
    private QueryPredicate predicate;
    private String signature;

    public static QueryParam parseFromJson(String jsonString) {
        QueryParam qp = JSONUtils.fromJson(jsonString, thisType);
        if (!EnvCache.devMode && !qp.validation())
            throw new SimplifiedException(ErrorAndExceptionCode.SIGNATURE_FAIL, "请求参数被客户端篡改");
        return qp;
    }

    /**
     * 解析order部分
     */
    public String parseOrder() {
        return predicate.getSort().length() > 0 ? "ORDER BY" + predicate.getSort().replaceAll("__", " ") : "";
    }

    /**
     * 验证查询条件是否被客户端篡改
     *
     * @return
     */
    public boolean validation() {
        String sign = predicate.calcSignature();
        if (logger.isDebugEnabled())
            logger.debug("the actual signature is: " + sign);
        return sign.equals(signature);
    }

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
     * 查询条件
     */
    public QueryPredicate getPredicate() {
        return predicate;
    }

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

    public int getStart() {
        return predicate.getStart();
    }

    public int getLimit() {
        return predicate.getLimit();
    }
}