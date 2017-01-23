package org.infrastructure.jpa.query;

/**
 * 自定义查询条件处理
 *
 * @author xuweinan
 */
public abstract class QueryParamHandler {
    private String field;

    public QueryParamHandler(String field) {
        this.field = field;
    }

    /**
     * 处理自定义查询条件
     *
     * @param dc  离线查询条件
     * @param val 参数值
     */
    public abstract void processCriteria(CriteriaBuilder dc, String val);

    public String getField() {
        return field;
    }
}