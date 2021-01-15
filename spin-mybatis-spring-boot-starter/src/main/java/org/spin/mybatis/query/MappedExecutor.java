package org.spin.mybatis.query;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * MyBatis条件执行器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/1/4</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public interface MappedExecutor<E, M extends BaseMapper<E>> {
    Logger logger = LoggerFactory.getLogger(MappedExecutor.class);

    M repo();

    Wrapper<E> getQuery();

    /**
     * 查询列表
     *
     * @return 记录列表
     */
    default List<E> list() {
        return repo().selectList(getQuery());
    }

    /**
     * 查询Map列表
     *
     * @return 记录列表
     */
    default List<Map<String, Object>> listMaps() {
        return repo().selectMaps(getQuery());
    }

    /**
     * 根据 Wrapper 条件，查询全部记录
     * <p>注意： 只返回第一个字段的值</p>
     *
     * @return 记录列表
     */
    default List<Object> listObjs() {
        return repo().selectObjs(getQuery());
    }

    /**
     * 查询单条记录，存在多个时返回第一个
     *
     * @return 实体(可能为null)
     */
    default E single() {
        List<E> list = list();
        if (CollectionUtils.isNotEmpty(list)) {
            int size = list.size();
            if (size > 1) {
                logger.warn(String.format("Warn: execute Method There are  %s results.", size));
            }
            return list.get(0);
        }
        return null;
    }

    /**
     * 查询单条记录，存在多条时，抛出异常
     *
     * @return 实体(可能为null)
     */
    default E unique() {
        return repo().selectOne(getQuery());
    }

    /**
     * 条件删除
     *
     * @return 删除数量
     */
    default int delete() {
        return repo().delete(getQuery());
    }

    /**
     * 条件更新
     *
     * @param entity 更新内容
     * @return 影响行数
     */
    default int update(E entity) {
        return repo().update(entity, getQuery());
    }

    /**
     * 统计数量
     *
     * @return 记录数
     */
    default Integer count() {
        return repo().selectCount(getQuery());
    }

    /**
     * 分页查询
     *
     * @param page 分页参数
     * @param <P>  Page类型
     * @return 分页结果
     */
    default <P extends IPage<E>> P page(P page) {
        return repo().selectPage(page, getQuery());
    }

    /**
     * 分页查询
     *
     * @param currentPage 当前页，从1开始
     * @param pageSize    分页大小
     * @return 分页结果
     */
    default Page<E> page(long currentPage, long pageSize) {
        Page<E> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        return page(page);
    }

    /**
     * 分页查询Map
     *
     * @param page 分页参数
     * @param <P>  分页参数类型
     * @return 分页结果
     */
    default <P extends IPage<Map<String, Object>>> P pageMaps(P page) {
        return repo().selectMapsPage(page, getQuery());
    }

    /**
     * 分页查询Map
     *
     * @param currentPage 当前页，从1开始
     * @param pageSize    分页大小
     * @return 分页结果
     */
    default Page<Map<String, Object>> pageMaps(long currentPage, long pageSize) {
        Page<Map<String, Object>> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        return repo().selectMapsPage(page, getQuery());
    }
}
