package org.infrastructure.util;

import javassist.util.proxy.ProxyFactory;
import org.hibernate.Hibernate;
import org.hibernate.collection.internal.PersistentBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 实体工具类
 *
 * @author zhou
 */
public abstract class EntityUtils {
    private static final Logger logger = LoggerFactory.getLogger(EntityUtils.class);

    /**
     * 将s对象属性，copy至d，遇到@Entity类型，只copy一层
     *
     * @param entity Hibernate的Entity实体
     * @param depth  copy层次
     * @return 返回一个实体
     */
    public static <T> T getDto(final T entity, final int depth) {
        if (entity == null)
            return null;
        final Class dcls = entity.getClass();
        final Class<?> tcls = Hibernate.getClass(entity);
        final T target;
        try {
            target = (T) tcls.newInstance();
        } catch (Exception e) {
            logger.error("Can not create new Entity instance:[" + tcls.getName() + "]", e);
            return null;
        }
        ReflectionUtils.doWithFields(dcls, f -> {
            String getM = f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
            Method getMethod = ReflectionUtils.findMethod(tcls, (f.getType().equals(boolean.class) || f.getType().equals(Boolean.class) ? "is" : "get") + getM);
            Method setMethod = ReflectionUtils.findMethod(tcls, "set" + getM, f.getType());
            if (setMethod == null || getMethod == null)
                return;
            try {
                if (depth <= 0) {
                    if (f.getType().equals(List.class))
                        setMethod.invoke(target, new ArrayList());
                    return;
                }
                if (f.getType().equals(List.class)) {
                    Object d_ = getMethod.invoke(entity);
                    if (d_ != null && d_ instanceof PersistentBag) {
                        PersistentBag bag = (PersistentBag) d_;
                        bag.clearDirty();
                        Object[] array = bag.toArray();
                        List list = Arrays.stream(array).map(obj -> ProxyFactory.isProxyClass(obj.getClass()) ? getDto(obj, 0) : obj).collect(Collectors.toList());
                        setMethod.invoke(target, list);
                    }
                } else if (f.getType().getAnnotation(Entity.class) != null)
                    setMethod.invoke(target, getDto(getMethod.invoke(entity), depth - 1));
                else
                    setMethod.invoke(target, getMethod.invoke(entity));
            } catch (InvocationTargetException e) {
                logger.error("Copy entity field error:[" + tcls.getName() + "|" + f.getName() + "]", e);
            }
        });
        return target;
    }

    /**
     * 得到主键字段
     *
     * @param entityClass 实体类
     * @return 主键字段
     */
    public static Field getPKField(Class entityClass) {
        final Field[] fs = new Field[1];
        ReflectionUtils.doWithFields(entityClass, f -> {
            if (f.getAnnotation(Id.class) != null)
                fs[0] = f;
        });
        return fs[0];
    }

    /**
     * 获取实体主键值
     */
    public static Object getPK(Object en) {
        Field opkF = EntityUtils.getPKField(en.getClass());
        String getM = opkF.getName().substring(0, 1).toUpperCase() + opkF.getName().substring(1);
        Method getMethod = ReflectionUtils.findMethod(en.getClass(), (opkF.getType().equals(boolean.class) || opkF.getType().equals(Boolean.class) ? "is" : "get") + getM);
        return ReflectionUtils.invokeMethod(getMethod, en);
    }

    /**
     * copy属性到另一个字段
     */
    public static void copyTo(Object src, Object dest, String... fields) {
        if (src == null || dest == null)
            return;
        for (String field : fields) {
            Field f1 = ReflectionUtils.findField(src.getClass(), field);
            Field f2 = ReflectionUtils.findField(dest.getClass(), field);
            if (f1 == null)
                throw new RuntimeException(field + "不存在于" + src.getClass().getSimpleName());
            if (f2 == null)
                throw new RuntimeException(field + "不存在于" + dest.getClass().getSimpleName());
            ReflectionUtils.makeAccessible(f1);
            ReflectionUtils.makeAccessible(f2);
            Object o1 = ReflectionUtils.getField(f1, src);
            ReflectionUtils.setField(f2, dest, o1);
        }
    }

    public static Object getFieldValue(Object src, String valuePath) {
        String[] valuePaths = valuePath.split("\\.");
        Object o = src;
        for (String field : valuePaths) {
            Field f = ReflectionUtils.findField(src.getClass(), field);
            ReflectionUtils.makeAccessible(f);
            o = ReflectionUtils.getField(f, o);
        }
        return o;
    }
}