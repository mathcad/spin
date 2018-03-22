package org.spin.data.query;

/**
 * 自定义查询条件处理
 * <p>Created by xuweinan on 2016/12/14.</p>
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

    /**
     * 获取处理字段
     *
     * @return 处理字段
     */
    public String getField() {
        return field;
    }
}
