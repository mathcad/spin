package org.spin.jpa.util;

import org.hibernate.Hibernate;
import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.collection.internal.PersistentList;
import org.hibernate.collection.internal.PersistentSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.SpinContext;
import org.spin.core.throwable.CloneFailedException;
import org.spin.core.util.BeanUtils;
import org.spin.core.util.ClassUtils;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.ReflectionUtils;
import org.spin.core.util.StringUtils;
import org.spin.data.core.IEntity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 实体工具类
 *
 * @author xuweinan
 */
public abstract class EntityUtils {
    private static final Logger logger = LoggerFactory.getLogger(EntityUtils.class);

    private EntityUtils() {
    }

    /**
     * 将实体转换为dto。遇到最内层@Entity类型，只带出id
     *
     * @param entity 标记有的Entity注解的Hibernate实体代理
     * @param depth  copy层次
     * @param <T>    实体类型
     * @return 返回一个实体
     */
    @SuppressWarnings("unchecked")
    public static <T> T getDTO(final T entity, final int depth) {
        if (entity == null) {
            return null;
        }
        final Class<?> tcls = Hibernate.getClass(entity);
        if (null == tcls.getAnnotation(Entity.class)) {
            return entity;
        }
        final T target;
        try {
            //noinspection unchecked
            target = (T) tcls.getConstructor().newInstance();
        } catch (Exception e) {
            logger.error("Can not create new Entity instance:[" + tcls.getName() + "]", e);
            return null;
        }

        ReflectionUtils.doWithFields(tcls, f -> {
            String getM = StringUtils.capitalize(f.getName());
            Method getMethod = ReflectionUtils.findMethod(tcls, (f.getType().equals(boolean.class) ? "is" : "get") + getM);
            Method setMethod = ReflectionUtils.findMethod(tcls, "set" + getM, f.getType());
            if (setMethod == null || getMethod == null) {
                return;
            }
            try {
                if (depth <= 0) {
                    if (f.getAnnotation(Id.class) != null) {
                        setMethod.invoke(target, getMethod.invoke(entity));
                    }
                    return;
                }
                if (f.getType().getAnnotation(Entity.class) != null) {
                    setMethod.invoke(target, getDTO(getMethod.invoke(entity), depth - 1));
                } else if (Objects.nonNull(ClassUtils.wrapperToPrimitive(f.getType())) || CharSequence.class.isAssignableFrom(f.getType())) {
                    // 基本类型或字符串，直接赋值字段
                    ReflectionUtils.makeAccessible(f);
                    f.set(target, getMethod.invoke(entity));
                } else if (List.class.isAssignableFrom(f.getType())) {
                    Object d = getMethod.invoke(entity);
                    if (d instanceof PersistentBag) {
                        PersistentBag bag = (PersistentBag) d;
                        bag.clearDirty();
                        List<Object> list = (List<Object>) JsonUtils.fromJson("[]", f.getType());
                        //noinspection unchecked
                        bag.forEach(obj -> list.add(getDTO(obj, depth - 1)));
                        setMethod.invoke(target, list);
                    } else if (d instanceof PersistentList) {
                        PersistentList bag = (PersistentList) d;
                        bag.clearDirty();

                        List<Object> list = (List<Object>) JsonUtils.fromJson("[]", f.getType());
                        //noinspection unchecked
                        bag.forEach(obj -> list.add(getDTO(obj, depth - 1)));
                        setMethod.invoke(target, list);
                    } else {
                        setMethod.invoke(target, d);
                    }
                } else if (Set.class.isAssignableFrom(f.getType())) {
                    Object d = getMethod.invoke(entity);
                    if (d instanceof PersistentSet) {
                        PersistentSet pSet = (PersistentSet) d;
                        pSet.clearDirty();
                        Set<Object> set = (Set<Object>) JsonUtils.fromJson("[]", f.getType());
                        //noinspection unchecked
                        pSet.forEach(obj -> set.add(getDTO(obj, depth - 1)));
                        setMethod.invoke(target, set);
                    } else {
                        setMethod.invoke(target, d);
                    }
                } else {
                    // 其他类型，调用set方法
                    setMethod.invoke(target, getMethod.invoke(entity));
                }
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
    public static Field getPKField(Class<?> entityClass) {
        final Field[] fs = new Field[1];
        ReflectionUtils.doWithFields(entityClass, f -> {
            if (f.getAnnotation(Id.class) != null)
                fs[0] = f;
        });
        return fs[0];
    }

    /**
     * 获取实体主键值
     *
     * @param en 实体
     * @return 主键值
     */
    public static Object getPK(Object en) {
        Field opkF = EntityUtils.getPKField(en.getClass());
        String getM = opkF.getName().substring(0, 1).toUpperCase() + opkF.getName().substring(1);
        Method getMethod = ReflectionUtils.findMethod(en.getClass(), (opkF.getType().equals(boolean.class) || opkF.getType().equals(Boolean.class) ? "is" : "get") + getM);
        return ReflectionUtils.invokeMethod(getMethod, en);
    }


    /**
     * 复制实体
     *
     * @param src 源对象
     * @param <T> 实体类型
     * @return 复制后的对象
     */
    public static <T extends IEntity<?, T>> T copy(T src) {
        try {
            @SuppressWarnings("unchecked")
            Class<T> clazz = (Class<T>) src.getClass();
            T target = clazz.getConstructor().newInstance();
            BeanUtils.copyTo(src, target);
            return target;
        } catch (Exception e) {
            throw new CloneFailedException("对象复制失败", e);
        }
    }

    /**
     * 解析实体中所有映射到数据库列的字段
     *
     * @param entityClazz 实体类型
     * @return 映射到数据库的字段名集合
     */
    public static Set<String> parseEntityColumns(Class<?> entityClazz) {
        if (SpinContext.ENTITY_COLUMNS.containsKey(entityClazz.getName()))
            return SpinContext.ENTITY_COLUMNS.get(entityClazz.getName());

        Field[] fields = entityClazz.getDeclaredFields();
        Set<String> columns = Arrays.stream(fields).filter(EntityUtils::isDbColumn).map(Field::getName).collect(Collectors.toSet());
        Class<?> superClass = entityClazz.getSuperclass();
        while (null != superClass) {
            columns.addAll(parseEntityColumns(superClass));
            superClass = superClass.getSuperclass();
        }
        SpinContext.ENTITY_COLUMNS.put(entityClazz.getName(), columns);
        return columns;
    }

    /**
     * 获得引用类型的n对一的引用字段列表
     *
     * @param cls 对象类型
     * @return 字段列表
     */
    public static Map<String, Field> getJoinFields(final Class<?> cls) {
        String clsName = cls.getName();
        if (!SpinContext.ENTITY_SOMETOONE_JOIN_FIELDS.containsKey(clsName)) {
            Map<String, Field> referJoinFields = new HashMap<>();
            ReflectionUtils.doWithFields(cls,
                f -> referJoinFields.put(f.getName(), f),
                f -> f.getAnnotation(ManyToOne.class) != null || f.getAnnotation(OneToOne.class) != null);
            SpinContext.ENTITY_SOMETOONE_JOIN_FIELDS.put(cls.getName(), referJoinFields);
        }
        return SpinContext.ENTITY_SOMETOONE_JOIN_FIELDS.get(clsName);
    }

    /**
     * 判断字段是否是映射到数据库
     *
     * @param field 字段
     * @return 是否映射
     */
    private static boolean isDbColumn(Field field) {
        // 非静态，final，且没有通过Transient注解排除的普通成员变量
        return (0x18 & field.getModifiers()) == 0 && null == field.getAnnotation(Transient.class);
    }
}
