package org.spin.common.vo;

import org.spin.common.db.entity.AbstractEntity;
import org.spin.core.util.BeanUtils;

import java.io.Serializable;

public interface EntityWrapper<V, T extends AbstractEntity> extends Serializable {

    @SuppressWarnings("unchecked")
    default V fromEntity(T entity) {
        if (null == entity) {
            return null;
        }
        BeanUtils.copyTo(entity, this);
        return (V) this;
    }
}
