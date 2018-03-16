package org.spin.data.core;

import org.spin.core.util.StringUtils;

import java.io.Serializable;

/**
 * 分页参数，索引从1开始
 * <p>Created by xuweinan on 2017/5/4.</p>
 *
 * @author xuweinan
 */
public class PageRequest implements Serializable {
    private static final long serialVersionUID = 3820016886407002254L;

    private int page = 1;
    private int pageSize = 10000000;
    private String sort;

    /**
     * 创建一个默认的{@link PageRequest}, 分页参数索引从1开始
     */
    public PageRequest() {
    }

    /**
     * 创建一个新的{@link PageRequest}, 分页参数索引从1开始
     *
     * @param page     zero-based page index.
     * @param pageSize the pageSize of the page to be returned.
     */
    public PageRequest(int page, int pageSize) {
        if (page < 1) {
            throw new IllegalArgumentException("Page index must not be less than one!");
        }

        if (pageSize < 0) {
            throw new IllegalArgumentException("Page pageSize must not be less than zero!");
        }

        this.page = page;
        this.pageSize = pageSize;
    }

    /**
     * 解析order部分
     *
     * @param tableAlias 子查询别名
     * @return {@link String} 查询语句
     */
    public String parseOrder(String tableAlias) {
        return StringUtils.isNotEmpty(sort) ?
            "ORDER BY " + tableAlias + "." + sort.replaceAll(",\\s*", ", " + tableAlias + ".")
            : "";
    }

    /**
     * 根据page与pageSize计算偏移
     */
    public int getOffset() {
        return (page - 1) * pageSize;
    }

    /**
     * 分页页码，从1开始
     */
    public int getPage() {
        return page;
    }

    /**
     * 分页页码，从1开始
     */
    public void setPage(int page) {
        if (page < 1) {
            throw new IllegalArgumentException("Page index must not be less than one!");
        }
        this.page = page;
    }

    /**
     * 分页大小
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * 分页大小
     */
    public void setPageSize(int pageSize) {
        if (pageSize < 0) {
            throw new IllegalArgumentException("Page pageSize must not be less than zero!");
        }
        this.pageSize = pageSize;
    }

    /**
     * 多个排序字段用,隔开
     * name desc,id desc
     */
    public String getSort() {
        return sort;
    }

    /**
     * 多个排序字段用,隔开
     * name desc,id desc
     */
    public void setSort(String sort) {
        this.sort = sort;
    }
}
