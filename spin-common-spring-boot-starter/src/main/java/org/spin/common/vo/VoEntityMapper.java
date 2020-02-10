package org.spin.common.vo;

import org.spin.common.db.entity.AbstractEntity;
import org.spin.core.util.BeanUtils;

public interface VoEntityMapper<V, T extends AbstractEntity> {

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
}
