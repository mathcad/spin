package org.spin.data.extend;

import org.spin.core.ErrorCode;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.BeanUtils;
import org.spin.data.core.EntityConverter;
import org.spin.data.core.IEntity;
import org.spin.data.util.EntityUtils;

import java.util.Map;

/**
 * 将Map与实体的转换器
 * <p>Created by xuweinan on 2016/9/24.</p>
 *
 * @author xuweinan
 */
public class MapEntityConverter<E extends IEntity> implements EntityConverter<Map<String, Object>, E> {

    @Override
    public E parseToEntity(Class<E> entityClazz, Map<String, Object> value) {
        try {
            return EntityUtils.wrapperMapToBean(entityClazz, value);
        } catch (Exception e) {
            throw new SimplifiedException(ErrorCode.BEAN_CREATE_FAIL, e);
        }
    }

    @Override
    public Map<String, Object> parseFromEntity(E entity) {
        throw new UnsupportedOperationException("不支持将加实体转换为Map");
    }
}
