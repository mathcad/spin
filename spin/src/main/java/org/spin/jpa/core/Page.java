package org.spin.jpa.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页数据（列表使用)
 *
 * @param <T>
 */
public class Page<T> implements Serializable {
    private static final long serialVersionUID = -1433098389717460681L;
    private List<T> rows = new ArrayList<>();
    private Long total = 0L;
    private int pageSize;

    public Page() {
    }

    public Page(List<T> rows, Long total, int pageSize) {
        this.rows = rows;
        this.total = total;
        this.pageSize = pageSize;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
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