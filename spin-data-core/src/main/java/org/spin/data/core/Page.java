package org.spin.data.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * 分页数据（列表使用)
 * <p>Created by xuweinan on 2016/10/5.</p>
 *
 * @param <T> 数据类型
 * @author xuweinan
 */
public class Page<T> implements Serializable {
    private static final long serialVersionUID = -1433098389717460681L;

    /**
     * 数据
     */
    private List<T> records = Collections.emptyList();

    /**
     * 当前页码, 从1开始
     */
    private long current = 1L;

    /**
     * 总数
     */
    private long total = 0L;

    /**
     * 分页大小
     */
    private int size = 0;

    public Page() {
    }

    /**
     * 构造方法
     *
     * @param records 数据
     * @param current 当前页
     * @param total   总数
     * @param size    页面大小
     */
    public Page(List<T> records, long current, long total, int size) {
        this.records = records;
        this.current = current;
        this.total = total;
        this.size = size;
    }

    public <R> Page<R> map(Function<T, R> mapper) {
        List<R> rows = new ArrayList<>(records.size());
        for (T record : records) {
            rows.add(mapper.apply(record));
        }
        return new Page<>(rows, current, total, size);
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
