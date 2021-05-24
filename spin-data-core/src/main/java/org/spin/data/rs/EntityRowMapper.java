package org.spin.data.rs;

import net.sf.cglib.beans.BeanMap;
import org.spin.core.util.BeanUtils;
import org.spin.core.util.ConstructorUtils;
import org.spin.data.throwable.SQLError;
import org.spin.data.throwable.SQLException;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

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
    private final Constructor<E> accessibleConstructor;

    public EntityRowMapper(Class<E> type) {
        Constructor<E> accessibleConstructor = ConstructorUtils.getAccessibleConstructor(type, CONSTRUCTOR_ARGS);
        if (null == accessibleConstructor) {
            throw new SQLException(SQLError.OBJECT_INSTANCE_ERROR, "指定的实体类型没有默认构造方法");
        }
        this.accessibleConstructor = accessibleConstructor;
    }

    @Override
    public E apply(ColumnVisitor columnVisitor, int rowIdx) throws java.sql.SQLException {
        E result;
        try {
            result = accessibleConstructor.newInstance();
        } catch (Exception e) {
            throw new SQLException(SQLError.OBJECT_INSTANCE_ERROR);
        }
        BeanMap rootBeanMap = BeanMap.create(result);

        Map<String, BeanMap> beanMapMap = new HashMap<>();

        for (int i = 0; i < columnVisitor.getColumnCount(); i++) {
            String alias = columnVisitor.getColumnName(i);
            if (alias != null) {
                String[] ap = alias.split("\\.");
                if (ap.length > 1) {
                    BeanMap work = rootBeanMap;
                    for (int j = 0; j < ap.length - 1; j++) {
                        if (!beanMapMap.containsKey(ap[j])) {
                            Class<?> propertyType = (j == 0 ? rootBeanMap : beanMapMap.get(ap[j - 1])).getPropertyType(ap[j]);
                            Object o = BeanUtils.instantiateClass(propertyType);
                            int t = 0;
                            int idx = alias.indexOf('.');
                            while (t++ <= j) {
                                idx = alias.indexOf('.', idx);
                            }
                            String p = alias.substring(0, idx);
                            work.put(p, o);
                            beanMapMap.put(p, BeanMap.create(o));
                        }
                        work = beanMapMap.get(ap[j]);
                    }
                    work.put(ap[ap.length - 1], columnVisitor.getColumnValue(i));
                } else {
                    rootBeanMap.put(alias, columnVisitor.getColumnValue(i));
                }
            }
        }
        return result;
    }
}
