package org.spin.mybatis.vo;

import org.spin.core.util.BeanUtils;
import org.spin.data.core.IEntity;

import java.io.Serializable;
import java.util.function.Supplier;

public interface VoEntityMapper<V, PK extends Serializable, T extends IEntity<PK, T>> extends Serializable {

    /**
     * 将目标实体中的字段copy到当前vo
     *
     * @param entity 目标实体
     * @return 当前vo
     */
    @SuppressWarnings("unchecked")
    default V fromEntity(T entity) {
        BeanUtils.copyTo(entity, this);
        return (V) this;
    }

    /**
     * 将当前vo中的字段copy到目标实体
     *
     * @param entity 目标实体
     * @return 目标实体
     */
    default T toEntity(T entity) {
        BeanUtils.copyTo(this, entity);
        return entity;
    }

    default T mergeToEntity(T entity) {
        BeanUtils.copyTo(this, entity, null, (f, v) -> null != v, null);
        return entity;
    }

    /**
     * 将当前Vo对象中的属性复制到实体(属性copy，不能做类型转换)
     *
     * @param entitySupplier 实体提供者
     * @return 拷贝属性后的实体
     */
    default T toEntity(Supplier<T> entitySupplier) {
        if (null == entitySupplier) {
            return null;
        }
        T entity = entitySupplier.get();
        BeanUtils.copyTo(this, entity);
        return entity;
    }

    default T mergeToEntity(Supplier<T> entitySupplier) {
        if (null == entitySupplier) {
            return null;
        }
        T entity = entitySupplier.get();
        BeanUtils.copyTo(this, entity, null, (f, v) -> null != v, null);
        return entity;
    }
}
