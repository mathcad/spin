package org.spin.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;
import org.spin.core.function.serializable.BiConsumer;
import org.spin.core.function.serializable.Function;
import org.spin.core.throwable.SimplifiedException;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
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
 * Bean工具类
 * Created by xuweinan on 2016/8/15.
 */
public abstract class BeanUtils {
    private static final Logger logger = LoggerFactory.getLogger(BeanUtils.class);
    private static final Map<String, Map<String, PropertyDescriptorWrapper>> CLASS_PROPERTY_CACHE = new ConcurrentHashMap<>();

    private BeanUtils() {
    }

    public static Method tailMethod(Class<?> type, String name) {
        try {
            return type.getMethod(name, String.class, Object.class);
        } catch (NoSuchMethodException | SecurityException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * 通过类的默认构造方法创建一个实例
     *
     * @param className 类名
     * @param <T>       类类型
     * @return T的实例
     */
    public static <T> T instantiateClass(String className) {
        Assert.notEmpty(className, "Class Name must not be null");
        Class<T> clazz;
        try {
            //noinspection unchecked
            clazz = (Class<T>) ClassUtils.getClass(className);
        } catch (ClassNotFoundException e) {
            throw new SimplifiedException("未找到类:" + className);
        } catch (Exception e) {
            throw new SimplifiedException("类型不匹配" + className);
        }
        return instantiateClass(clazz);
    }

    /**
     * 通过类的默认构造方法创建一个实例
     *
     * @param clazz 类
     * @param <T>   类类型
     * @return T的实例
     */
    public static <T> T instantiateClass(Class<T> clazz) {
        if (Assert.notNull(clazz, "Class must not be null").isInterface()) {
            throw new SimplifiedException(clazz.getName() + " is an interface");
        }
        try {
            return instantiateClass(clazz.getDeclaredConstructor());
        } catch (NoSuchMethodException ex) {
            throw new SimplifiedException("No default constructor found", ex);
        }
    }

    /**
     * 调用指定构造方法创建一个实例
     *
     * @param ctor 构造方法
     * @param <T>  类类型
     * @param args 参数
     * @return T的实例
     */
    public static <T> T instantiateClass(Constructor<T> ctor, Object... args) {
        Assert.notNull(ctor, "Constructor must not be null");
        try {
            ReflectionUtils.makeAccessible(ctor);
            return ctor.newInstance(args);
        } catch (InstantiationException ex) {
            throw new SimplifiedException("Is " + ctor.getName() + " an abstract class?", ex);
        } catch (IllegalAccessException ex) {
            throw new SimplifiedException("Is the constructor " + ctor.getName() + " accessible?", ex);
        } catch (IllegalArgumentException ex) {
            throw new SimplifiedException("Illegal arguments for constructor " + ctor.getName(), ex);
        } catch (InvocationTargetException ex) {
            throw new SimplifiedException("Constructor " + ctor.getName() + " threw exception", ex.getTargetException());
        }
    }

    /**
     * 将properties中的属性通过setter设置到bean中，bean中不存在的属性将被忽略
     *
     * @param bean       目标对象
     * @param properties 属性properties
     */
    public static void applyProperties(Object bean, Map<?, ?> properties) {
        properties.forEach((key, value) -> {
            String getterName = "get" + StringUtils.capitalize(key.toString());
            String setterName = "set" + StringUtils.capitalize(key.toString());
            try {
                Object v = value;
                Class<?>[] args = {};
                Method getter = MethodUtils.getAccessibleMethod(bean.getClass(), getterName, args);
                if (Objects.nonNull(getter)) {
                    v = ObjectUtils.convert(getter.getReturnType(), value);
                }
                MethodUtils.invokeMethod(bean, setterName, v);
            } catch (NoSuchMethodException e) {
                logger.info("不存在属性[" + key + "]的set方法");
            } catch (IllegalAccessException e) {
                throw new SimplifiedException("属性[" + key + "]的set方法不允许访问");
            } catch (InvocationTargetException e) {
                throw new SimplifiedException("设置属性[" + key + "]失败", e);
            }
        });
    }

    /**
     * 将平面的Map转换成树状组织的Map
     *
     * @param values 扁平的Map
     * @return 树状Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> wrapperFlatMap(Map<String, Object> values) {
        Map<String, Object> treeSightMap = new HashMap<>();
        int off;
        int next;
        int depth;
        String p;
        String[] propName = new String[100];
        Map<String, Object> work;
        Map<String, Object> bak;
        Map<String, Object> tmp;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            off = 0;
            depth = 0;
            p = entry.getKey();
            while ((next = p.indexOf('.', off)) != -1) {
                propName[depth++] = p.substring(off, next);
                off = next + 1;
            }
            propName[depth++] = (p.substring(off));
            if (depth == 1) {
                treeSightMap.put(propName[0], entry.getValue());
                continue;
            }
            work = treeSightMap;
            int i = 0;
            while (depth != i) {
                if (i != depth - 1) {
                    tmp = (Map<String, Object>) work.get(propName[i]);
                    if (tmp != null) {
                        work = tmp;
                        ++i;
                        continue;
                    }
                    bak = new HashMap<>();
                    work.put(propName[i], bak);
                    work = bak;
                    ++i;
                } else {
                    work.put(propName[i], entry.getValue());
                    ++i;
                }
            }
        }
        return treeSightMap;
    }

    /**
     * 通过getter或者setter方法的名称，获取field名称
     *
     * @param getterOrSetter getter/setter方法名称
     * @return field名称
     */
    public static String toFieldName(String getterOrSetter) {
        if (null == getterOrSetter || getterOrSetter.length() < 2) {
            return getterOrSetter;
        }
        if (getterOrSetter.startsWith("get") || getterOrSetter.startsWith("set")) {
            return StringUtils.uncapitalize(getterOrSetter.substring(3));
        }
        if (getterOrSetter.startsWith("is")) {
            return StringUtils.uncapitalize(getterOrSetter.substring(2));
        }
        return getterOrSetter;
    }

    /**
     * 获取对象指定属性的值
     * <p>通过反射直接读取属性，不通过get方法。需要获取List,数组或Map中的元素，field请使用#开头。如obj.#name.#1</p>
     * <p>获取数组与List中的第n个元素：#n</p>
     * <p>获取中Map中键为key对应的value：#key</p>
     *
     * @param target    对象实例
     * @param valuePath 属性名称，支持嵌套
     * @param <T>       属性类型参数
     * @return 属性值
     */
    public static <T> T getFieldValue(Object target, String valuePath) {
        String[] valuePaths = Assert.notEmpty(valuePath, "valuePath必须指定属性名称").split("\\.");
        Object o = target;
        for (int i = 0; i < valuePaths.length; i++) {
            String field = valuePaths[i];
            char mark = field.charAt(0);

            if (i < valuePath.length() - 1 && null == o) {
                throw new SimplifiedException(field + "属性为null");
            }
            if ('#' == mark) {
                if (o instanceof Map) {
                    o = ((Map) o).get(field.substring(1));
                } else if (o instanceof List) {
                    int idx;
                    String f = field;
                    try {
                        f = field.substring(1);
                        idx = Integer.valueOf(f);
                    } catch (Exception e) {
                        throw new SimplifiedException(f + "索引不是有效的数字");
                    }
                    if (((List) o).size() < idx) {
                        throw new SimplifiedException(f + "索引超出范围0-" + ((List) o).size());
                    }
                    o = ((List) o).get(idx);
                } else if (o.getClass().isArray()) {
                    int idx;
                    String f = field;
                    try {
                        f = field.substring(1);
                        idx = Integer.valueOf(f);
                    } catch (Exception e) {
                        throw new SimplifiedException(f + "索引不是有效的数字");
                    }
                    @SuppressWarnings("ConstantConditions")
                    Object[] t = (Object[]) o;
                    if (t.length < idx) {
                        throw new SimplifiedException(f + "索引超出范围0-" + t.length);
                    }
                    o = t[idx];
                } else {
                    throw new SimplifiedException(o.getClass().toString() + "中的属性名称[" + field + "]不合法");
                }
            } else {
                Field f;
                try {
                    f = ReflectionUtils.findField(o.getClass(), field);
                    ReflectionUtils.makeAccessible(f);
                } catch (Exception e) {
                    throw new SimplifiedException(o.getClass().toString() + "不存在" + field + "属性", e);
                }

                try {
                    o = ReflectionUtils.getField(f, o);
                } catch (Exception e) {
                    throw new SimplifiedException(o.getClass().toString() + "获取属性" + field + "的值失败", e);
                }
            }
        }
        //noinspection unchecked
        return (T) o;
    }

    /**
     * 获取对象指定属性的值
     *
     * @param target 对象实例
     * @param fields 取值的属性名称
     * @return 属性map
     */
    public static Map<String, Object> getFieldValue(Object target, String... fields) {
        return getFieldValue(target, Arrays.asList(fields));

    }

    /**
     * 获取对象指定属性的值
     *
     * @param target 对象实例
     * @param fields 取值的属性名称
     * @return 属性map
     */
    public static Map<String, Object> getFieldValue(Object target, Collection<String> fields) {
        Map<String, Object> result = new HashMap<>();
        if (CollectionUtils.isEmpty(fields)) {
            return new HashMap<>(0);
        }
        for (String field : fields) {
            Object value = getFieldValue(target, field);
            result.put(field, value);
        }
        return result;
    }

    /**
     * JavaBean转换为Map
     *
     * @param target            bean
     * @param camelToUnderscore 是否将驼峰命名转换为下划线命名方式
     * @return map
     */
    public static Map<String, String> toStringMap(Object target, boolean camelToUnderscore) {
        // null值，直接返回
        if (null == target) {
            return null;
        }

        // 如果是Map，做适应性调整
        if (target instanceof Map) {
            Map<?, ?> m = (Map<?, ?>) target;
            return m.entrySet().stream().filter(entry -> null != entry.getValue()).collect(Collectors.toMap(e -> camelToUnderscore ? StringUtils.underscore(e.getKey().toString()) : e.getKey().toString(), e -> StringUtils.toString(e.getValue())));
        }

        if (isJavaBean(target)) {
            Collection<PropertyDescriptorWrapper> props = getBeanPropertyDes(target.getClass(), true, false).values();
            Map<String, String> res = new HashMap<>(props.size());
            for (PropertyDescriptorWrapper prop : props) {
                try {
                    Object value = prop.reader.invoke(target);
                    if (null != value) {
                        res.put(camelToUnderscore ? StringUtils.underscore(prop.getDescriptor().getName()) : prop.getDescriptor().getName(), StringUtils.toString(value));
                    }
                } catch (IllegalAccessException | InvocationTargetException ignore) {
                    // 忽略访问失败的属性
                }
            }
            return res;
        }

        throw new SimplifiedException(target.getClass().getName() + "不能转换为Map<String, String>");
    }

    /**
     * JavaBean转换为Map(字段名转换为下划线形式)
     *
     * @param target bean
     * @return map
     */
    public static Map<String, String> toUnderscoreStringMap(Object target) {
        return toStringMap(target, true);
    }

    /**
     * JavaBean转换为Map
     *
     * @param target bean
     * @return map
     */
    public static Map<String, String> toStringMap(Object target) {
        return toStringMap(target, false);
    }

    /**
     * JavaBean转换为Map
     *
     * @param target      bean
     * @param recursively 是否递归处理所有属性
     * @return map
     */
    public static Map<String, Object> toMap(Object target, boolean recursively) {
        Object result = toMapInternal(target, recursively);
        if (result instanceof Map || null == result) {
            //noinspection unchecked
            return (Map<String, Object>) result;
        } else {
            throw new SimplifiedException(target.getClass().getName() + "不能被转换为Map");
        }
    }


    private static Object toMapInternal(Object target, boolean recursively) {
        // null值，直接返回
        if (null == target) {
            return null;
        }

        // 如果是Map，做适应性调整
        if (target instanceof Map) {
            Map<?, ?> m = (Map<?, ?>) target;
            if (recursively) {
                return m.entrySet().stream().filter(entry -> null != entry.getValue()).collect(Collectors.toMap(e -> e.getKey().toString(), e -> BeanUtils.toMapInternal(e.getValue(), true)));
            } else {
                return m.entrySet().stream().filter(entry -> null != entry.getValue()).collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
            }
        }

        // 如果是集合，将其元素转为Map后返回
        if (CollectionUtils.isCollection(target)) {
            List objects = CollectionUtils.asList(target);
            for (int i = 0; i < objects.size(); i++) {
                //noinspection unchecked
                objects.set(i, toMapInternal(objects.get(i), recursively));
            }
            return objects;
        }

        // 如果是JavaBean，将其转换为Map
        if (isJavaBean(target)) {
            Collection<PropertyDescriptorWrapper> props = getBeanPropertyDes(target.getClass(), true, false).values();
            Map<String, Object> res = new HashMap<>(props.size());
            for (PropertyDescriptorWrapper prop : props) {
                try {
                    Object value = prop.reader.invoke(target);
                    if (null != value) {
                        res.put(prop.getDescriptor().getName(), toMapInternal(value, recursively));
                    }
                } catch (IllegalAccessException | InvocationTargetException ignore) {
                    // 忽略访问失败的属性
                }
            }
            return res;
        } else {
            // 否则原样返回
            return target;
        }

    }

    /**
     * 判断一个对象是否是JavaBean
     * <p>一个JavaBean，一定是一个自定义对象（非java自带的类，数组，集合，Map，枚举，字符序列)</p>
     *
     * @param target 对象
     * @return 是否是JavaBean
     */
    public static boolean isJavaBean(Object target) {
        return null != target && !target.getClass().isArray() && !target.getClass().getName().startsWith("java.") &&
            !target.getClass().getName().startsWith("javax.") && !(target instanceof Map || target instanceof Collection ||
            target instanceof Enum || target instanceof CharSequence);
    }

    /**
     * 通过内省机制，获取一个JavaBean的所有属性(必须可写)
     *
     * @param type JavaBean类型
     * @return 属性描述器数组
     */
    public static Map<String, PropertyDescriptorWrapper> getBeanPropertyDes(Class<?> type) {
        Map<String, PropertyDescriptorWrapper> props = CLASS_PROPERTY_CACHE.get(type.getName());
        if (null == props) {
            PropertyDescriptor[] propertyDescriptors = propertyDescriptors(type);
            props = new HashMap<>();
            Method writer;
            for (PropertyDescriptor descriptor : propertyDescriptors) {
                writer = descriptor.getWriteMethod();
                if (writer != null)
                    props.put(descriptor.getName().toLowerCase(), new PropertyDescriptorWrapper(descriptor, descriptor.getReadMethod(), writer));
            }
            CLASS_PROPERTY_CACHE.put(type.getName(), props);
        }
        return props;
    }

    /**
     * 通过内省机制，获取一个JavaBean的所有属性
     *
     * @param type     JavaBean类型
     * @param readable 是否必须可读
     * @param writable 是否必须可写
     * @return 属性描述器数组
     */
    public static Map<String, PropertyDescriptorWrapper> getBeanPropertyDes(Class<?> type, boolean readable, boolean writable) {
        Map<String, PropertyDescriptorWrapper> props = CLASS_PROPERTY_CACHE.get(type.getName() + readable + writable);
        if (null == props) {
            PropertyDescriptor[] propertyDescriptors = propertyDescriptors(type);
            props = new HashMap<>();
            Method writer;
            Method reader;
            for (PropertyDescriptor descriptor : propertyDescriptors) {
                writer = descriptor.getWriteMethod();
                reader = descriptor.getReadMethod();
                if (!("class".equals(descriptor.getName()) && Class.class == descriptor.getPropertyType()) && (!readable || reader != null) && (!writable || writer != null)) {
                    props.put(descriptor.getName().toLowerCase(), new PropertyDescriptorWrapper(descriptor, reader, writer));
                }
            }
            CLASS_PROPERTY_CACHE.put(type.getName() + readable + writable, props);
        }
        return props;
    }

    /**
     * 通过内省机制获取一个JavaBean的所有属性的getter方法
     *
     * @param c JavaBean类型
     * @return getter方法集合
     */
    public static List<Method> resolveGetters(Class<?> c) {
        Collection<PropertyDescriptorWrapper> ps;
        List<Method> list = new ArrayList<>();
        ps = getBeanPropertyDes(c, true, false).values();
        for (PropertyDescriptorWrapper p : ps) {
            if (p.reader != null) {
                list.add(p.reader);
            }
        }
        return list;
    }

    /**
     * 通过内省机制，获取一个JavaBean的所有属性
     *
     * @param c JavaBean类型
     * @return 属性描述器数组
     */
    private static PropertyDescriptor[] propertyDescriptors(Class<?> c) {
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(c);
        } catch (IntrospectionException e) {
            throw new SimplifiedException("解析Bean属性异常", e);
        }
        return beanInfo.getPropertyDescriptors();
    }

    /**
     * 属性描述器
     */
    public static class PropertyDescriptorWrapper {
        public PropertyDescriptor descriptor;
        public Class<?> protertyType;
        public Method reader;
        public Method writer;

        public PropertyDescriptorWrapper(PropertyDescriptor descriptor, Method reader, Method writer) {
            this.descriptor = descriptor;
            this.protertyType = descriptor.getPropertyType();
            this.reader = reader;
            this.writer = writer;
        }

        public PropertyDescriptorWrapper(PropertyDescriptor descriptor) {
            this.descriptor = descriptor;
            this.protertyType = descriptor.getPropertyType();
            this.reader = descriptor.getReadMethod();
            this.writer = descriptor.getWriteMethod();
        }

        public PropertyDescriptor getDescriptor() {
            return descriptor;
        }

        public void setDescriptor(PropertyDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        public Class<?> getProtertyType() {
            return protertyType;
        }

        public void setProtertyType(Class<?> protertyType) {
            this.protertyType = protertyType;
        }

        public Method getReader() {
            return reader;
        }

        public void setReader(Method reader) {
            this.reader = reader;
        }

        public Method getWriter() {
            return writer;
        }

        public void setWriter(Method writer) {
            this.writer = writer;
        }
    }

    /**
     * 复制JavaBean的属性到另一个JavaBean中，直接反射字段值，不通过getter/setter
     * <p>如果不指定字段列表，并且源对象是JavaBean，则会拷贝所有字段</p>
     *
     * @param src    源实体
     * @param dest   目标实体
     * @param fields 字段名列表
     */
    public static void copyTo(Object src, Object dest, String... fields) {
        if (null == src || null == dest) {
            return;
        }
        if ((null == fields || fields.length == 0) && isJavaBean(src)) {
            Set<String> srcFields = getBeanPropertyDes(src.getClass(), false, false).values().stream().map(p -> p.getDescriptor().getName()).collect(Collectors.toSet());
            fields = getBeanPropertyDes(dest.getClass(), false, false).values().stream().map(p -> p.getDescriptor().getName()).filter(srcFields::contains).toArray(String[]::new);
        } else {
            throw new SimplifiedException("非JavaBean请指定需要Copy的属性列表");
        }
        for (String field : fields) {
            Field f1 = ReflectionUtils.findField(src.getClass(), field);
            Field f2 = ReflectionUtils.findField(dest.getClass(), field);
            if (f1 == null)
                throw new SimplifiedException(field + "不存在于" + src.getClass().getSimpleName());
            if (f2 == null)
                throw new SimplifiedException(field + "不存在于" + dest.getClass().getSimpleName());
            ReflectionUtils.makeAccessible(f1);
            ReflectionUtils.makeAccessible(f2);
            Object o1 = ReflectionUtils.getField(f1, src);
            ReflectionUtils.setField(f2, dest, o1);
        }
    }

    /**
     * 复制JavaBean的属性到另一个JavaBean中，直接反射字段值，不通过getter/setter
     *
     * @param src     源对象
     * @param dest    目标对象
     * @param getters 属性getter列表
     */
    public static <T> void copyTo(T src, Object dest, Iterable<Function<T, ?>> getters) {
        if (null == src || null == dest || null == getters)
            return;
        for (Function<T, ?> field : getters) {
            String fieldName = toFieldName(LambdaUtils.resolveLambda(field).getImplMethodName());
            Field f1 = ReflectionUtils.findField(src.getClass(), fieldName);
            Field f2 = ReflectionUtils.findField(dest.getClass(), fieldName);
            if (f1 == null) {
                throw new SimplifiedException(field + "不存在于" + src.getClass().getSimpleName());
            }
            if (f2 == null) {
                throw new SimplifiedException(field + "不存在于" + dest.getClass().getSimpleName());
            }
            ReflectionUtils.makeAccessible(f1);
            ReflectionUtils.makeAccessible(f2);
            Object o1 = ReflectionUtils.getField(f1, src);
            ReflectionUtils.setField(f2, dest, o1);
        }
    }

    public static <T, V, P> void copyTo(T src, V dest, Function<T, P> getter, BiConsumer<V, P> setter) {
        if (null == src || null == dest || null == getter || null == setter)
            return;
        setter.accept(dest, getter.apply(src));
    }

    public static <T, V, P1, P2>
    void copyTo(T src, V dest,
                Function<T, P1> getter1, BiConsumer<V, P1> setter1,
                Function<T, P2> getter2, BiConsumer<V, P2> setter2
    ) {
        if (null == src || null == dest)
            return;

        if (null != getter1 && null != setter1) {
            setter1.accept(dest, getter1.apply(src));
        }

        if (null != getter2 && null != setter2) {
            setter2.accept(dest, getter2.apply(src));
        }
    }

    public static <T, V, P1, P2, P3>
    void copyTo(T src, V dest,
                Function<T, P1> getter1, BiConsumer<V, P1> setter1,
                Function<T, P2> getter2, BiConsumer<V, P2> setter2,
                Function<T, P3> getter3, BiConsumer<V, P3> setter3
    ) {
        if (null == src || null == dest)
            return;

        if (null != getter1 && null != setter1) {
            setter1.accept(dest, getter1.apply(src));
        }

        if (null != getter2 && null != setter2) {
            setter2.accept(dest, getter2.apply(src));
        }

        if (null != getter3 && null != setter3) {
            setter3.accept(dest, getter3.apply(src));
        }
    }


    public static <T, V, P1, P2, P3, P4>
    void copyTo(T src, V dest,
                Function<T, P1> getter1, BiConsumer<V, P1> setter1,
                Function<T, P2> getter2, BiConsumer<V, P2> setter2,
                Function<T, P3> getter3, BiConsumer<V, P3> setter3,
                Function<T, P4> getter4, BiConsumer<V, P4> setter4
    ) {
        if (null == src || null == dest)
            return;

        if (null != getter1 && null != setter1) {
            setter1.accept(dest, getter1.apply(src));
        }

        if (null != getter2 && null != setter2) {
            setter2.accept(dest, getter2.apply(src));
        }

        if (null != getter3 && null != setter3) {
            setter3.accept(dest, getter3.apply(src));
        }

        if (null != getter4 && null != setter4) {
            setter4.accept(dest, getter4.apply(src));
        }
    }

    public static <T, V, P1, P2, P3, P4, P5>
    void copyTo(T src, V dest,
                Function<T, P1> getter1, BiConsumer<V, P1> setter1,
                Function<T, P2> getter2, BiConsumer<V, P2> setter2,
                Function<T, P3> getter3, BiConsumer<V, P3> setter3,
                Function<T, P4> getter4, BiConsumer<V, P4> setter4,
                Function<T, P5> getter5, BiConsumer<V, P5> setter5
    ) {
        if (null == src || null == dest)
            return;

        if (null != getter1 && null != setter1) {
            setter1.accept(dest, getter1.apply(src));
        }

        if (null != getter2 && null != setter2) {
            setter2.accept(dest, getter2.apply(src));
        }

        if (null != getter3 && null != setter3) {
            setter3.accept(dest, getter3.apply(src));
        }

        if (null != getter4 && null != setter4) {
            setter4.accept(dest, getter4.apply(src));
        }

        if (null != getter5 && null != setter5) {
            setter5.accept(dest, getter5.apply(src));
        }
    }

    public static <T, V, P1, P2, P3, P4, P5, P6>
    void copyTo(T src, V dest,
                Function<T, P1> getter1, BiConsumer<V, P1> setter1,
                Function<T, P2> getter2, BiConsumer<V, P2> setter2,
                Function<T, P3> getter3, BiConsumer<V, P3> setter3,
                Function<T, P4> getter4, BiConsumer<V, P4> setter4,
                Function<T, P5> getter5, BiConsumer<V, P5> setter5,
                Function<T, P6> getter6, BiConsumer<V, P6> setter6
    ) {
        if (null == src || null == dest)
            return;

        if (null != getter1 && null != setter1) {
            setter1.accept(dest, getter1.apply(src));
        }

        if (null != getter2 && null != setter2) {
            setter2.accept(dest, getter2.apply(src));
        }

        if (null != getter3 && null != setter3) {
            setter3.accept(dest, getter3.apply(src));
        }

        if (null != getter4 && null != setter4) {
            setter4.accept(dest, getter4.apply(src));
        }

        if (null != getter5 && null != setter5) {
            setter5.accept(dest, getter5.apply(src));
        }

        if (null != getter6 && null != setter6) {
            setter6.accept(dest, getter6.apply(src));
        }
    }

    public static <T, V, P1, P2, P3, P4, P5, P6, P7>
    void copyTo(T src, V dest,
                Function<T, P1> getter1, BiConsumer<V, P1> setter1,
                Function<T, P2> getter2, BiConsumer<V, P2> setter2,
                Function<T, P3> getter3, BiConsumer<V, P3> setter3,
                Function<T, P4> getter4, BiConsumer<V, P4> setter4,
                Function<T, P5> getter5, BiConsumer<V, P5> setter5,
                Function<T, P6> getter6, BiConsumer<V, P6> setter6,
                Function<T, P7> getter7, BiConsumer<V, P7> setter7
    ) {
        if (null == src || null == dest)
            return;

        if (null != getter1 && null != setter1) {
            setter1.accept(dest, getter1.apply(src));
        }

        if (null != getter2 && null != setter2) {
            setter2.accept(dest, getter2.apply(src));
        }

        if (null != getter3 && null != setter3) {
            setter3.accept(dest, getter3.apply(src));
        }

        if (null != getter4 && null != setter4) {
            setter4.accept(dest, getter4.apply(src));
        }

        if (null != getter5 && null != setter5) {
            setter5.accept(dest, getter5.apply(src));
        }

        if (null != getter6 && null != setter6) {
            setter6.accept(dest, getter6.apply(src));
        }

        if (null != getter7 && null != setter7) {
            setter7.accept(dest, getter7.apply(src));
        }
    }

    public static <T, V, P1, P2, P3, P4, P5, P6, P7, P8>
    void copyTo(T src, V dest,
                Function<T, P1> getter1, BiConsumer<V, P1> setter1,
                Function<T, P2> getter2, BiConsumer<V, P2> setter2,
                Function<T, P3> getter3, BiConsumer<V, P3> setter3,
                Function<T, P4> getter4, BiConsumer<V, P4> setter4,
                Function<T, P5> getter5, BiConsumer<V, P5> setter5,
                Function<T, P6> getter6, BiConsumer<V, P6> setter6,
                Function<T, P7> getter7, BiConsumer<V, P7> setter7,
                Function<T, P8> getter8, BiConsumer<V, P8> setter8
    ) {
        if (null == src || null == dest)
            return;

        if (null != getter1 && null != setter1) {
            setter1.accept(dest, getter1.apply(src));
        }

        if (null != getter2 && null != setter2) {
            setter2.accept(dest, getter2.apply(src));
        }

        if (null != getter3 && null != setter3) {
            setter3.accept(dest, getter3.apply(src));
        }

        if (null != getter4 && null != setter4) {
            setter4.accept(dest, getter4.apply(src));
        }

        if (null != getter5 && null != setter5) {
            setter5.accept(dest, getter5.apply(src));
        }

        if (null != getter6 && null != setter6) {
            setter6.accept(dest, getter6.apply(src));
        }

        if (null != getter7 && null != setter7) {
            setter7.accept(dest, getter7.apply(src));
        }

        if (null != getter8 && null != setter8) {
            setter8.accept(dest, getter8.apply(src));
        }
    }

    public static <T, V, P1, P2, P3, P4, P5, P6, P7, P8, P9>
    void copyTo(T src, V dest,
                Function<T, P1> getter1, BiConsumer<V, P1> setter1,
                Function<T, P2> getter2, BiConsumer<V, P2> setter2,
                Function<T, P3> getter3, BiConsumer<V, P3> setter3,
                Function<T, P4> getter4, BiConsumer<V, P4> setter4,
                Function<T, P5> getter5, BiConsumer<V, P5> setter5,
                Function<T, P6> getter6, BiConsumer<V, P6> setter6,
                Function<T, P7> getter7, BiConsumer<V, P7> setter7,
                Function<T, P8> getter8, BiConsumer<V, P8> setter8,
                Function<T, P9> getter9, BiConsumer<V, P9> setter9
    ) {
        if (null == src || null == dest)
            return;

        if (null != getter1 && null != setter1) {
            setter1.accept(dest, getter1.apply(src));
        }

        if (null != getter2 && null != setter2) {
            setter2.accept(dest, getter2.apply(src));
        }

        if (null != getter3 && null != setter3) {
            setter3.accept(dest, getter3.apply(src));
        }

        if (null != getter4 && null != setter4) {
            setter4.accept(dest, getter4.apply(src));
        }

        if (null != getter5 && null != setter5) {
            setter5.accept(dest, getter5.apply(src));
        }

        if (null != getter6 && null != setter6) {
            setter6.accept(dest, getter6.apply(src));
        }

        if (null != getter7 && null != setter7) {
            setter7.accept(dest, getter7.apply(src));
        }

        if (null != getter8 && null != setter8) {
            setter8.accept(dest, getter8.apply(src));
        }

        if (null != getter9 && null != setter9) {
            setter9.accept(dest, getter9.apply(src));
        }
    }
}
