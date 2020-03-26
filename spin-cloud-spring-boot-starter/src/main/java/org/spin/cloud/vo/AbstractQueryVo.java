package org.spin.cloud.vo;

import io.swagger.annotations.ApiModelProperty;
import org.spin.core.util.StringUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * 抽象查询参数
 * <p>包含排序功能</p>
 * <p>Created by xuweinan on 2019/12/12</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public abstract class AbstractQueryVo implements Serializable {

    @ApiModelProperty(value = "排序字段", example = "name")
    private String orderBy;

    @ApiModelProperty(value = "是否升序, 默认为true", example = "true")
    private Boolean asc = true;

    public boolean hasOrder() {
        return StringUtils.isNotBlank(orderBy);
    }

    public String getOrder() {
        return "`" + StringUtils.underscore(orderBy) + "` " + (Boolean.FALSE.equals(asc) ? "DESC" : "ASC");
    }

    public String getOrderBy() {
        return StringUtils.underscore(orderBy);
    }

    public String getOriginOrder() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public Boolean getAsc() {
        return null == asc || asc;
    }

    public void setAsc(Boolean asc) {
        this.asc = asc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractQueryVo that = (AbstractQueryVo) o;
        return Objects.equals(orderBy, that.orderBy) &&
            Objects.equals(asc, that.asc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderBy, asc);
    }
}
