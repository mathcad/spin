package org.infrastructure.util;

import org.hibernate.Hibernate;
import org.hibernate.collection.internal.PersistentBag;
import org.infrastructure.jpa.core.GenericUser;
import org.infrastructure.sys.EnvCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;


/**
 * 反射复制对象（提供不支持深度copy）
 *
 * @author zhou
 */
public class ElUtils {
    static final Logger logger = LoggerFactory.getLogger(ElUtils.class);

    public Object val$(Object v, Object v1) {
        if (v == null)
            return v1;
        if (v instanceof Boolean && !((Boolean) v))
            return v1;

        return v;
    }

    /**
     * 将s对象属性，copy至d，遇到@Entity类型，只copy一层
     *
     * @param d             Hibernate的Entity实体
     * @param depth         copy层次
     * @param includeFields 必须copy的字段（，分割多字段）
     * @return 返回一个实体
     */
    public static <T> T getDto(final T d, final int depth, final String... includeFields) {
        final HashSet<String> includeFieldSet = new HashSet<>();
        if (includeFields != null) {
            Collections.addAll(includeFieldSet, includeFields);
        }

        if (d == null)
            return null;
        final Class dcls = d.getClass();
        final Class<?> tcls = Hibernate.getClass(d);
        try {
            final Object t = tcls.newInstance();

            ReflectionUtils.doWithFields(dcls, f -> {
                try {
                    String getM = f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
                    Method getMethod = ReflectionUtils.findMethod(tcls, (f.getType().equals(boolean.class) ? "is" : "get") + getM);
                    Method setMethod = ReflectionUtils.findMethod(tcls, "set" + getM, f.getType());

                    //如果在包含字段中
                    if (includeFieldSet.contains(f.getName())) {
                        setMethod.invoke(t, getMethod.invoke(d));
                        return;
                    }

                    //无属性方法
                    if (setMethod == null || getMethod == null)
                        return;
                    /* list集合延迟加载 */
                    if (f.getType().equals(List.class)) {
                        if (depth > 0) {
                            Object d_ = getMethod.invoke(d);
                            if (d_ != null && d_ instanceof PersistentBag) {
                                PersistentBag bag = (PersistentBag) d_;
                                bag.clearDirty();
                                Object[] array = bag.toArray();
                                List list = new ArrayList();
                                for (Object o : array) {
                                    if (javassist.util.proxy.ProxyFactory.isProxyClass(o.getClass())) {
                                        o = getDto(o, 0);
                                    }
                                    list.add(o);
                                }
                                setMethod.invoke(t, list);
                            }
                        } else {
                            List list = new ArrayList();
                            setMethod.invoke(t, list);
                        }
                    } else if (f.getType().isAssignableFrom(GenericUser.class) || f.getType().getAnnotation(Entity.class) != null) {
                        /* Entity关联按层次需求来级联copy */
                        if (depth > 0) {
                            Object d_ = getMethod.invoke(d);
                            setMethod.invoke(t, getDto(d_, depth - 1, includeFields));
                        } else {
                            Object d_ = f.getType().newInstance();
                        }
                    } else {
                        /* 其他简单类型 copy赋值 */
                        setMethod.invoke(t, getMethod.invoke(d));
                    }
                } catch (Exception e) {
                    logger.error("", e);
                }
            });

            return (T) t;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
            if (f.getAnnotation(Id.class) != null) {
                fs[0] = f;
            }
        });
        return fs[0];
    }

    /**
     * 获取实体主键值
     */
    public static Object getPK(Object en) {
        Field opkF = ElUtils.getPKField(en.getClass());
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
