package org.spin.mybatis.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.spin.core.Assert;
import org.spin.core.OpResult;
import org.spin.core.gson.annotation.PreventOverflow;
import org.spin.core.util.BeanUtils;
import org.spin.data.core.IEntity;
import org.spin.mybatis.query.LambdaQueryExecutor;
import org.spin.mybatis.query.R;

import java.io.Serializable;

/**
 * 基础实体
 * <p>定义了数据库实体的基本字段, 原则上所有实体均应直接或间接继承{@link BasicEntity}</p>
 * <p>Created by xuweinan on 2019/9/19</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public abstract class BasicEntity<T extends BasicEntity<T>> implements IEntity<Long, T>, Serializable {

    // region properties
    /**
     * 主键
     */
    @TableId
    @PreventOverflow
    private Long id;

    /**
     * 数据版本
     */
    @Version
    private Integer version = 0;

    @Override
    public Long id() {
        return id;
    }

    @Override
    public void id(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Integer getVersion() {
        return version;
    }

    @Override
    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public Boolean getValid() {
        return true;
    }

    @Override
    public void setValid(Boolean aBoolean) {
        // do nothing
    }
    // endregion

    /**
     * 将当前实体新增至数据库
     *
     * @return 当前对象本身
     */
    public OpResult<T> insert() {
        @SuppressWarnings("unchecked")
        T e = (T) this;
        int cnt = repo().insert(e);
        return OpResult.of(e, cnt == 1);
    }

    /**
     * 根据ID将当前实体更新到数据库
     *
     * @return 当前对象本身
     */
    public OpResult<Integer> updateById() {
        @SuppressWarnings("unchecked")
        T e = (T) this;
        Assert.notNull(e.getId(), "ID不能为空");
        int cnt = repo().updateById(e);
        return OpResult.of(cnt, cnt == 1);
    }

    /**
     * 根据ID将当前实体从数据库删除
     *
     * @return 影响行数
     */
    public OpResult<Integer> deleteById() {
        @SuppressWarnings("unchecked")
        T e = (T) this;
        return OpResult.of(repo().deleteById(Assert.notNull(e.getId(), "ID不能为空")), i -> i == 1);
    }

    /**
     * 根据ID查询实体并合并到当前对象
     *
     * @return 实体包装
     */
    public OpResult<T> loadById() {
        @SuppressWarnings("unchecked")
        T e = (T) this;
        T inDb = repo().selectById(Assert.notNull(e.getId(), "ID不能为空"));
        e.mergeAll(inDb);
        return OpResult.of(e, inDb != null);
    }

    @SuppressWarnings("unchecked")
    private BaseMapper<T> repo() {
        return R.repo((T) this);
    }

    /**
     * 根据id,获取一个持有该id的指定类型的DTO对象
     *
     * @param id     id
     * @param ignore 用来获取类型, 无实际意义
     * @param <E>    实体类型泛型参数
     * @return 持有指定id的DTO对象
     */
    @SuppressWarnings("unchecked")
    public static <E extends BasicEntity<E>> E refId(Long id, E... ignore) {
        Assert.notNull(ignore, "类型参数不能为null");
        @SuppressWarnings("unchecked")
        E entity = (E) BeanUtils.instantiateClass(ignore.getClass().getComponentType());
        entity.setId(id);
        return entity;
    }

    /**
     * 获取查询上下文
     *
     * @param ignore 用来获取类型, 无实际意义
     * @param <E>    实体类型
     * @return 查询条件上下文
     */
    @SafeVarargs
    public static <E extends BasicEntity<E>> LambdaQueryExecutor<E> query(E... ignore) {
        return R.query(ignore);
    }
}
