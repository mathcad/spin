package org.spin.data.util;

import javassist.util.proxy.ProxyFactory;
import org.hibernate.Hibernate;
import org.hibernate.collection.internal.PersistentBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.SpinContext;
import org.spin.core.throwable.CloneFailedException;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.ClassUtils;
import org.spin.core.util.ObjectUtils;
import org.spin.core.util.ReflectionUtils;
import org.spin.core.util.StringUtils;
import org.spin.data.core.IEntity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 实体工具类
 *
 * @author xuweinan
 */
public abstract class EntityUtils {
    private static final Logger logger = LoggerFactory.getLogger(EntityUtils.class);
    private static final Map<String, Map<String, EntityUtils.PropertyDescriptorWrapper>> CLASS_PROPERTY_CACHE = new ConcurrentHashMap<>();

    /**
     * 将s对象属性，copy至d，遇到@Entity类型，只copy一层
     *
     * @param entity Hibernate的Entity实体
     * @param depth  copy层次
     * @return 返回一个实体
     */
    public static <T> T getDTO(final T entity, final int depth) {
        if (entity == null)
            return null;
        final Class<?> tcls = Hibernate.getClass(entity);
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
            if (setMethod == null || getMethod == null)
                return;
            try {
                if (depth <= 0) {
                    if (f.getType().equals(List.class)) {
                        setMethod.invoke(target, new ArrayList());
                    } else if (f.getAnnotation(Id.class) != null) {
                        setMethod.invoke(target, getMethod.invoke(entity));
                    }
                    return;
                }
                if (f.getType().equals(List.class)) {
                    Object d_ = getMethod.invoke(entity);
                    if (d_ != null && d_ instanceof PersistentBag) {
                        PersistentBag bag = (PersistentBag) d_;
                        bag.clearDirty();
                        Object[] array = bag.toArray();
                        List list = Arrays.stream(array).map(obj -> ProxyFactory.isProxyClass(obj.getClass()) ? getDTO(obj, 0) : obj).collect(Collectors.toList());
                        setMethod.invoke(target, list);
                    }
                } else if (f.getType().getAnnotation(Entity.class) != null) {
                    setMethod.invoke(target, getDTO(getMethod.invoke(entity), depth - 1));
                } else if (Objects.nonNull(ClassUtils.wrapperToPrimitive(f.getType())) || CharSequence.class.isAssignableFrom(f.getType())) {
                    // 基本类型或字符串，直接赋值字段
                    ReflectionUtils.makeAccessible(f);
                    f.set(target, getMethod.invoke(entity));
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
        if (null == src || null == dest || null == fields)
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

    /**
     * copy属性到另一个字段
     */
    public static void copyTo(Object src, Object dest, Collection<String> fields) {
        if (null == src || null == dest || null == fields)
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

    /**
     * 复制实体
     *
     * @param src 源对象
     * @param <T> 实体类型
     */
    public static <T extends IEntity> T copy(T src) {
        try {
            @SuppressWarnings("unchecked")
            Class<T> clazz = (Class<T>) src.getClass();
            T target = clazz.getConstructor().newInstance();
            Set<String> fields = Arrays.stream(clazz.getFields()).map(Field::getName).collect(Collectors.toSet());
            copyTo(src, target, fields);
            return target;
        } catch (Exception e) {
            throw new CloneFailedException("对象复制失败", e);
        }
    }

    /**
     * 解析实体中所有映射到数据库列的字段
     */
    public static Set<String> parseEntityColumns(Class entityClazz) {
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
     * @return 字段列表
     */
    public static Map<String, Field> getJoinFields(final Class cls) {
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
     * 将Map转换为对象
     * <p>
     * 复合属性，请在语句中指定别名为实体属性的路径。如createUser.id对应createUser的id属性。<br>
     * 如果Map中存在某些Key不能与实体的属性对应，将被舍弃。
     * </p>
     */
    public static <T> T wrapperMapToBean(Class<T> type, Map<String, Object> values) throws IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException, NoSuchMethodException {
        T bean = type.getDeclaredConstructor().newInstance();
        Map<String, EntityUtils.PropertyDescriptorWrapper> props = CLASS_PROPERTY_CACHE.get(type.getName());
        if (null == props) {
            PropertyDescriptor[] propertyDescriptors = propertyDescriptors(type);
            props = new HashMap<>();
            Method writer;
            for (PropertyDescriptor descriptor : propertyDescriptors) {
                writer = descriptor.getWriteMethod();
                if (writer != null)
                    props.put(descriptor.getName().toLowerCase(), new EntityUtils.PropertyDescriptorWrapper(descriptor, writer));
            }
            CLASS_PROPERTY_CACHE.put(type.getName(), props);
            CLASS_PROPERTY_CACHE.put(type.getName(), props);
        }
        if (props.size() == 0)
            return bean;
        int off;
        int next;
        int depth;
        String p;
        String[] propName = new String[100];
        Object[] args = new Object[1];
        Map<String, EntityUtils.PropertyDescriptorWrapper> workerProps;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            off = 0;
            depth = 0;
            p = entry.getKey().toLowerCase();
            while ((next = p.indexOf('.', off)) != -1) {
                propName[depth++] = p.substring(off, next);
                off = next + 1;
            }
            propName[depth++] = (p.substring(off));
            if (depth == 1) {
                EntityUtils.PropertyDescriptorWrapper prop = props.get(p);
                if (null == prop)
                    continue;
                args[0] = ObjectUtils.convert(prop.protertyType, entry.getValue());
                prop.writer.invoke(bean, args);
                continue;
            }
            int i = 0;
            Object worker = bean;
            Class<?> propType;
            workerProps = getBeanPropertyDes(type);
            while (depth != i) {
                EntityUtils.PropertyDescriptorWrapper prop = workerProps.get(propName[i]);
                if (null == prop) {
                    ++i;
                    continue;
                }
                propType = prop.protertyType;
                if (i != depth - 1 && IEntity.class.isAssignableFrom(propType)) {
                    Object ib = prop.reader == null ? null : prop.reader.invoke(worker);
                    if (null == ib) {
                        ib = propType.getDeclaredConstructor().newInstance();
                        args[0] = ObjectUtils.convert(propType, ib);
                        prop.writer.invoke(worker, args);
                    }
                    workerProps = getBeanPropertyDes(propType);
                    worker = ib;
                    ++i;
                    continue;
                }
                if (i == depth - 1) {
                    args[0] = ObjectUtils.convert(propType, entry.getValue());
                    prop.writer.invoke(worker, args);
                }
                ++i;
            }
        }
        return bean;
    }

    public static <T> List<T> wrapperMapToBeanList(Class<T> type, List<Map<String, Object>> values) {
        return values.stream().map(m -> {
            try {
                return wrapperMapToBean(type, m);
            } catch (Exception e) {
                throw new SimplifiedException("Bean convert fail");
            }
        }).collect(Collectors.toList());
    }


    /**
     * 将Map转换为对象
     * <p>
     * 复合属性，请在语句中指定别名为实体属性的路径。如createUser.id对应createUser的id属性。<br>
     * 如果Map中存在某些Key不能与实体的属性对应，将被舍弃。
     * </p>
     * 由于递归实现效率低下，不建议使用
     */
    @Deprecated
    public static <T> T wrapperMapToBean(Class<T> type, Map<String, Object> values, String propPrefix) throws IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException {
        T bean = type.newInstance();
        Map<String, PropertyDescriptorWrapper> props = CLASS_PROPERTY_CACHE.get(type.getName());
        if (null == props) {
            PropertyDescriptor[] propertyDescriptors = propertyDescriptors(type);
            if (null == propertyDescriptors || 0 == propertyDescriptors.length)
                return type.newInstance();
            props = new HashMap<>();
            for (PropertyDescriptor descriptor : propertyDescriptors) {
                Method writer = descriptor.getWriteMethod();
                if (writer != null)
                    props.put(descriptor.getName().toLowerCase(), new PropertyDescriptorWrapper(descriptor, writer));
            }
            CLASS_PROPERTY_CACHE.put(type.getName(), props);
        }

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (StringUtils.isNotEmpty(propPrefix) && !entry.getKey().toLowerCase().startsWith(propPrefix))
                continue;
            String propName = StringUtils.isEmpty(propPrefix) ? entry.getKey().toLowerCase() : entry.getKey().substring(propPrefix.length() + 1).toLowerCase();
            int index = propName.indexOf('.');
            if (index > 0) {
                propName = propName.substring(0, index);
                if (props.containsKey(propName)) {
                    PropertyDescriptorWrapper prop = props.get(propName);
                    Object tmp = wrapperMapToBean(prop.protertyType, values, StringUtils.isEmpty(propPrefix) ? propName : propPrefix + "." + propName);
                    Object[] args = new Object[1];
                    args[0] = tmp;
                    prop.writer.invoke(bean, args);
                }
            } else {
                if (props.containsKey(propName)) {
                    PropertyDescriptorWrapper prop = props.get(propName);
                    Object[] args = new Object[1];
                    args[0] = ObjectUtils.convert(prop.protertyType, entry.getValue());
                    prop.writer.invoke(bean, args);
                }
            }
        }
        return bean;
    }

    public static Map<String, PropertyDescriptorWrapper> getBeanPropertyDes(Class<?> type) throws IntrospectionException {
        Map<String, PropertyDescriptorWrapper> props = CLASS_PROPERTY_CACHE.get(type.getName());
        if (null == props) {
            PropertyDescriptor[] propertyDescriptors = propertyDescriptors(type);
            props = new HashMap<>();
            Method writer;
            for (PropertyDescriptor descriptor : propertyDescriptors) {
                writer = descriptor.getWriteMethod();
                if (writer != null)
                    props.put(descriptor.getName().toLowerCase(), new PropertyDescriptorWrapper(descriptor, writer));
            }
            CLASS_PROPERTY_CACHE.put(type.getName(), props);
        }
        return props;
    }

    public static PropertyDescriptor[] propertyDescriptors(Class<?> c) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(c);
        return beanInfo.getPropertyDescriptors();
    }

    public static List<Method> getterMethod(Class<?> c) throws IntrospectionException {
        PropertyDescriptor[] ps;
        List<Method> list = new ArrayList<>();
        ps = propertyDescriptors(c);
        for (PropertyDescriptor p : ps) {
            if (p.getReadMethod() != null && p.getWriteMethod() != null) {
                list.add(p.getReadMethod());
            }
        }
        return list;
    }

    /**
     * 判断字段是否是映射到数据库
     */
    private static boolean isDbColumn(Field field) {
        Annotation[] annotations = field.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof Transient) {
                return false;
            }
        }
        return true;
    }

    public static class PropertyDescriptorWrapper {
        public PropertyDescriptor descriptor;
        public Class<?> protertyType;
        public Method reader;
        public Method writer;

        public PropertyDescriptorWrapper(PropertyDescriptor descriptor, Method writer) {
            this.descriptor = descriptor;
            this.protertyType = descriptor.getPropertyType();
            this.reader = descriptor.getReadMethod();
            this.writer = writer;
        }
    }
}
