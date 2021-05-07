package org.spin.data.core;

import org.spin.core.util.CollectionUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 分页参数，索引从1开始
 * <p>Created by xuweinan on 2017/5/4.</p>
 *
 * @author xuweinan
 */
public class PageRequest implements Serializable {
    private static final long serialVersionUID = 3820016886407002254L;

    /**
     * 分页页码，从1开始
     */
    private int current = 1;

    /**
     * 分页大小
     */
    private int size = 10000;

    /**
     * 是否进行count查询
     */
    private boolean searchCount = true;

    /**
     * 多个排序字段用,隔开
     * name desc,id desc
     */
    private List<Order> sort;

    /**
     * 创建一个默认的{@link PageRequest}, 分页参数索引从1开始
     */
    public PageRequest() {
    }

    /**
     * 创建一个新的{@link PageRequest}, 分页参数索引从1开始
     *
     * @param current one-based page index.
     * @param size    the pageSize of the page to be returned.
     */
    public PageRequest(int current, int size) {
        if (current < 1) {
            throw new IllegalArgumentException("Page index must not be less than one!");
        }

        if (size < 0) {
            throw new IllegalArgumentException("Page pageSize must not be less than zero!");
        }

        this.current = current;
        this.size = size;
    }

    /**
     * 解析order部分
     *
     * @param tableAlias 子查询别名
     * @return {@link String} 查询语句
     */
    public String parseOrder(String tableAlias) {
        if (CollectionUtils.isNotEmpty(sort)) {
            StringBuilder sb = new StringBuilder("ORDER BY ");
            for (Order order : sort) {
                sb.append(tableAlias).append(".").append(order.toString()).append(",");
            }

            return sb.substring(0, sb.length() - 1);
        }
        return "";
    }

    /**
     * 根据page与pageSize计算偏移
     *
     * @return 偏移量
     */
    public int getOffset() {
        return (current - 1) * size;
    }


    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current < 1) {
            throw new IllegalArgumentException("Page index must not be less than one!");
        }
        this.current = current;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Page pageSize must not be less than zero!");
        }
        this.size = size;
    }

    public boolean isSearchCount() {
        return searchCount;
    }

    public void setSearchCount(boolean searchCount) {
        this.searchCount = searchCount;
    }

    public List<Order> getSort() {
        return sort;
    }

    public void setSort(List<Order> sort) {
        this.sort = sort;
    }
}
