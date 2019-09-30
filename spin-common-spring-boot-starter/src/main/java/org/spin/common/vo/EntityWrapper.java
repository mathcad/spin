package org.spin.common.vo;

import org.spin.common.db.entity.BasicEntity;
import org.spin.core.util.BeanUtils;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * 实体转换器
 *
 * @param <V> Vo类型
 * @param <T> 实体类型
 */
public interface EntityWrapper<V, T extends BasicEntity> extends Serializable {

    /**
     * 将实体中的属性复制到当前Vo对象(属性copy，不能做类型转换)
     *
     * @param entity 实体
     * @return 当前Vo对象
     */
    @SuppressWarnings("unchecked")
    default V copyFromEntity(T entity) {
        if (null == entity) {
            return null;
        }
        BeanUtils.copyTo(entity, this);
        return (V) this;
    }

    /**
     * 将当前Vo对象中的属性复制到实体(属性copy，不能做类型转换)
     *
     * @param entity 实体
     * @return 拷贝属性后的实体
     */
    default T copyToEntity(T entity) {
        if (null != entity) {
            BeanUtils.copyTo(this, entity);
        }
        return entity;
    }

    /**
     * 将当前Vo对象中的属性复制到实体(属性copy，不能做类型转换)
     *
     * @param entitySupplier 实体提供者
     * @return 拷贝属性后的实体
     */
    default T copyToEntity(Supplier<T> entitySupplier) {
        if (null == entitySupplier) {
            return null;
        }
        T entity = entitySupplier.get();
        if (null != entity) {
            BeanUtils.copyTo(this, entity);
        }
        return entity;
    }
}
