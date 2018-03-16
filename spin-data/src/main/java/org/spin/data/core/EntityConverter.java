package org.spin.data.core;

/**
 * 实体解析器接口
 * <p>Created by xuweinan on 2016/9/24.</p>
 *
 * @author xuweinan
 */
public interface EntityConverter<T, E extends IEntity> {

    /**
     * 将用户自定义类型转换为实体
     *
     * @param entityClazz 实体类型
     * @param value       用户自定义数据
     * @return 实体
     */
    E parseToEntity(Class<E> entityClazz, T value);

    /**
     * 将实体转换为用户自定义类型
     *
     * @param entity 实体
     * @return 用户自定义数据
     */
    T parseFromEntity(E entity);
}
