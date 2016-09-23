package org.infrastructure.jpa.core;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

/**
 * 分页数据（列表使用)
 *
 * @param <T>
 */
public class Page<T> implements Serializable {
    private static final long serialVersionUID = -1433098389717460681L;
    private List<T> data;
    private Long total;
    private int pageSize;

    public Page() {
    }

    public Page(List<T> data, Long total, int pageSize) {
        this.data = data;
        this.total = total;
        this.pageSize = pageSize;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}