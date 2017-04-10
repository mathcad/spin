package org.spin.jpa.extend;

import org.spin.jpa.core.EntityParser;
import org.spin.sys.ErrorCode;
import org.spin.throwable.SimplifiedException;
import org.spin.util.BeanUtils;

import java.util.Map;

/**
 * 将Map对象解析实体
 * Created by xuweinan on 2016/9/24.
 *
 * @author xuweinan
 */
public class MapToEntityParser implements EntityParser<Map<String, Object>> {
    @Override
    public <T> T parseToEntity(Class<T> entityClazz, Map<String, Object> value) {
        try {
            return BeanUtils.wrapperMapToBean(entityClazz, value);
        } catch (Exception e) {
            throw new SimplifiedException(ErrorCode.BEAN_CREATE_FAIL, e);
        }
    }
}
