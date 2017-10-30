package org.spin.data.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页数据（列表使用)
 * <p>Created by xuweinan on 2016/10/5.</p>
 *
 * @param <T>
 * @author xuweinan
 */
public class Page<T> implements Serializable {
    private static final long serialVersionUID = -1433098389717460681L;

    private List<T> rows = new ArrayList<>();
    private long total = 0L;
    private int pageSize = 0;

    public Page() {
    }

    /**
     * 构造方法
     *
     * @param rows     数据
     * @param total    总数
     * @param pageSize 页面大小
     */
    public Page(List<T> rows, long total, int pageSize) {
        this.rows = rows;
        this.total = total;
        this.pageSize = pageSize;
    }

    /**
     * 数据
     */
    public List<T> getRows() {
        return rows;
    }

    /**
     * 数据
     */
    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    /**
     * 总数
     */
    public long getTotal() {
        return total;
    }

    /**
     * 总数
     */
    public void setTotal(long total) {
        this.total = total;
    }

    /**
     * 页面大小
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * 页面大小
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
