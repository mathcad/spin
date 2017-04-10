package org.spin.jpa.core;

/**
 * 实体解析器接口
 * Created by xuweinan on 2016/9/24.
 *
 * @author xuweinan
 */
public interface EntityParser<E> {
    <T> T parseToEntity(Class<T> entityClazz, E value);
}
