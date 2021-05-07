package org.spin.jpa.entity;

import org.spin.core.throwable.SimplifiedException;
import org.spin.core.trait.FriendlyEnum;

import javax.persistence.AttributeConverter;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * JPA枚举转换器, 搭配{@link javax.persistence.Convert}使用
 * <p>所有需要自定义映射的JPA实体枚举都需要实现该接口</p>
 * <p>Created by xuweinan on 2021/4/1</p>
 *
 * @author xuweinan
 * @version 1.0
 */

public abstract class JpaEnumConverter<E extends FriendlyEnum<V>, V extends Serializable> implements AttributeConverter<E, V> {

    private final Class<E> enumClass;

    public JpaEnumConverter(Class<E> enumClass) {
        if (!enumClass.isEnum()) {
            throw new SimplifiedException("枚举转换器声明不合法");
        }
        this.enumClass = enumClass;
    }

    @SuppressWarnings("unchecked")
    protected JpaEnumConverter() {
        if (this.getClass().getGenericSuperclass() instanceof ParameterizedType) {
            ParameterizedType superclass = (ParameterizedType) this.getClass().getGenericSuperclass();
            Type enumType = superclass.getActualTypeArguments()[0];
            if (enumType instanceof Class) {
                this.enumClass = (Class<E>) enumType;
                if (!enumClass.isEnum()) {
                    throw new SimplifiedException("枚举转换器声明不合法");
                }
            } else {
                throw new SimplifiedException("枚举转换器声明不合法");
            }
        } else {
            throw new SimplifiedException("枚举转换器声明不合法");
        }
    }

    @Override
    public V convertToDatabaseColumn(E attribute) {
        return null == attribute ? null : attribute.getValue();
    }

    @Override
    public E convertToEntityAttribute(V dbData) {
        return null == dbData ? null : FriendlyEnum.valueOf(enumClass, dbData).orElse(null);
    }
}
