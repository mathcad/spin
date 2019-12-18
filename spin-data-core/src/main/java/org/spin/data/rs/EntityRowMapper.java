package org.spin.data.rs;

import org.spin.core.util.BeanUtils;
import org.spin.core.util.ConstructorUtils;
import org.spin.data.throwable.SQLError;
import org.spin.data.throwable.SQLException;

import java.lang.reflect.Constructor;

/**
 * ResultSet到实体的转换器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/10/6</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class EntityRowMapper<E> implements RowMapper<E> {

    private static final Class<?>[] CONSTRUCTOR_ARGS = new Class[0];
    private final Class<E> type;
    private final Constructor<E> accessibleConstructor;

    public EntityRowMapper(Class<E> type) {
        Constructor<E> accessibleConstructor = ConstructorUtils.getAccessibleConstructor(type, CONSTRUCTOR_ARGS);
        if (null == accessibleConstructor) {
            throw new SQLException(SQLError.OBJECT_INSTANCE_ERROR, "指定的实体类型没有默认构造方法");
        }
        this.type = type;
        this.accessibleConstructor = accessibleConstructor;
    }

    @Override
    public E apply(String[] columnNames, Object[] columns, int columnCount, int rowIdx) {
        E entity;
        try {
            entity = accessibleConstructor.newInstance();
        } catch (Exception e) {
            throw new SQLException(SQLError.OBJECT_INSTANCE_ERROR);
        }
        BeanUtils.applyProperties(entity, columnNames, columns);
        return entity;
    }
}
