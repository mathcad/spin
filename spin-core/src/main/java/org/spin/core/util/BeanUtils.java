package org.spin.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;
import org.spin.core.collection.Tuple;
import org.spin.core.function.serializable.Function;
import org.spin.core.function.serializable.Supplier;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.throwable.SpinException;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
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
            throw new SpinException("未找到类:" + className);
        } catch (Exception e) {
            throw new SpinException("类型不匹配" + className);
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
            throw new SpinException(clazz.getName() + " is an interface");
        }
        try {
            return instantiateClass(clazz.getDeclaredConstructor());
        } catch (NoSuchMethodException ex) {
            throw new SpinException("No default constructor found", ex);
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
            throw new SpinException("Is " + ctor.getName() + " an abstract class?", ex);
        } catch (IllegalAccessException ex) {
            throw new SpinException("Is the constructor " + ctor.getName() + " accessible?", ex);
        } catch (IllegalArgumentException ex) {
            throw new SpinException("Illegal arguments for constructor " + ctor.getName(), ex);
        } catch (InvocationTargetException ex) {
            throw new SpinException("Constructor " + ctor.getName() + " threw exception", ex.getTargetException());
        }
    }

    /**
     * 将properties中的属性通过setter设置到bean中，bean中不存在的属性将被忽略
     *
     * @param bean       目标对象
     * @param properties 属性properties
     */
    public static void applyProperties(Object bean, Map<String, ?> properties) {
        Map<String, BeanUtils.PropertyDescriptorWrapper> props = BeanUtils.getBeanPropertyDes(bean.getClass());
        if (props.size() == 0) {
            return;
        }

        properties.forEach((propName, propVal) -> {
            if (props.containsKey(propName)) {
                props.get(propName).applyProperty(bean, propVal);
            }
        });
    }

    /**
     * 将properties中的属性通过setter设置到bean中，bean中不存在的属性将被忽略
     *
     * @param bean        目标对象
     * @param fieldNames  属性名称数组
     * @param fieldValues 属性值数组
     */
    public static void applyProperties(Object bean, String[] fieldNames, Object[] fieldValues) {
        Assert.isTrue(fieldNames.length == fieldValues.length, "属性名称与属性值个数不一致");
        int columnCount = fieldNames.length;

        Map<String, BeanUtils.PropertyDescriptorWrapper> props = BeanUtils.getBeanPropertyDes(bean.getClass());
        if (props.size() == 0) {
            return;
        }

        for (int i = 0; i < columnCount; i++) {
            String propName = fieldNames[i];
            Object propVal = fieldValues[i];
            if (props.containsKey(propName)) {
                props.get(propName).applyProperty(bean, propVal);
            }
        }
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
     * <p>通过反射直接读取属性, 如果字段不存在, 则会查找get方法.
     * 数组, {@link Iterable}, {@link Tuple}等可迭代类型通过"[idx]"索引位置访问，Map中的元素可以直接访问，如果需要访问{@link Map}对象中的成员变量,
     * 需要在变量名前加"#", 如map.#size</p>
     * <p>获取数组与List等可迭代类型中的第n个元素：list[n], 高维数组(嵌套集合): list[x][y][z]</p>
     * <p>获取Map中键为key对应的value：map.key</p>
     * <p>获取Map中名称为size的成员变量的值：map.#size</p>
     *
     * @param target    对象实例
     * @param valuePath 属性名称，支持嵌套
     * @param <T>       属性类型参数
     * @return 属性值
     */
    public static <T> T getFieldValue(Object target, String valuePath) {
        String[] valuePaths = Assert.notEmpty(valuePath, "valuePath必须指定属性名称").split("\\.");
        if (null == target) {
            return null;
        }
        Object o = target;
        for (int i = 0; i < valuePaths.length; i++) {
            String field = valuePaths[i];
            List<Integer> seqs = new LinkedList<>();
            if (field.indexOf('[') != -1) {
                final StringBuilder f = new StringBuilder(field.length());
                final StringBuilder seq = new StringBuilder(field.length());
                boolean inPos = false;
                for (char c : field.toCharArray()) {
                    switch (c) {
                        case '[':
                            if (inPos) {
                                throw new SpinException("索引表达式未正确结束: " + field);
                            }
                            inPos = true;
                            break;
                        case ']':
                            if (!inPos) {
                                throw new SpinException("索引表达式未正确开始: " + field);
                            }
                            try {
                                seqs.add(Integer.parseInt(seq.toString()));
                            } catch (NumberFormatException ignore) {
                                throw new SpinException("索引必须是合法的数字: " + field);
                            }
                            seq.setLength(0);
                            inPos = false;
                            break;
                        case '-':
                            if (inPos) {
                                throw new SpinException("索引不能为负数: " + field);
                            }
                        default:
                            if (inPos) {
                                seq.append(c);
                            } else {
                                f.append(c);
                            }
                            break;
                    }
                }
                field = f.toString();
                if (i != 0 && f.length() == 0) {
                    throw new SpinException("表达式不合法，未指定索引的对象" + field);
                }
            }

            if (field.length() > 0) {
                char mark = field.charAt(0);

                if (i < valuePath.length() - 1 && null == o) {
                    throw new SpinException(field + "属性为null");
                }
                if ('#' != mark && o instanceof Map) {
                    o = ((Map) o).get(field);
                } else {
                    Field f;
                    if (mark == '#') {
                        field = field.substring(1);
                    }
                    try {
                        f = ReflectionUtils.findField(o.getClass(), field);
                        ReflectionUtils.makeAccessible(f);
                        o = ReflectionUtils.getField(f, o);
                    } catch (Exception e) {
                        String getterName = "get" + StringUtils.capitalize(field);
                        try {
                            o = MethodUtils.invokeMethod(o, getterName, null);
                        } catch (Exception ex) {
                            throw new SpinException(o.getClass().toString() + "不存在" + field + "属性", ex);
                        }
                    }
                }
            }

            for (Integer idx : seqs) {
                if (null == o) {
                    throw new SpinException(field + "属性为null");
                }

                if (o instanceof List) {
                    if (((List) o).size() <= idx) {
                        throw new SpinException(idx + " 索引超出范围0-" + ((List) o).size());
                    }
                    o = ((List) o).get(idx);
                } else if (o.getClass().isArray()) {
                    @SuppressWarnings("ConstantConditions")
                    Object[] t = (Object[]) o;
                    if (t.length <= idx) {
                        throw new SpinException(idx + " 索引超出范围0-" + t.length);
                    }
                    o = t[idx];
                } else if (o instanceof Collection) {
                    Collection<?> t = (Collection<?>) o;
                    if (t.size() <= idx) {
                        throw new SpinException(idx + " 索引超出范围0-" + t.size());
                    }
                    int k = 0;
                    for (Object obj : t) {
                        if (k == idx) {
                            o = obj;
                            break;
                        }
                        ++k;
                    }
                } else if (o instanceof Tuple) {
                    Tuple<?> t = (Tuple<?>) o;

                    if (t.size() <= idx) {
                        throw new SpinException(idx + " 索引超出范围0-" + t.size());
                    }
                    o = t.get(idx);
                } else if (o instanceof Iterable) {
                    Iterable<?> t = (Iterable<?>) o;

                    int k = 0;
                    for (Object obj : t) {
                        if (k == idx) {
                            o = obj;
                            break;
                        }
                        ++k;
                    }
                    if (k <= idx) {
                        throw new SpinException(idx + " 索引超出范围0-" + k);
                    }
                } else {
                    throw new SpinException(o.getClass().toString() + "不支持索引方式访问");
                }
            }
        }
        //noinspection unchecked
        return (T) o;
    }

    /**
     * 设置对象指定属性的值
     * <p>通过反射直接设置属性.
     * 数组, {@link List}可以通过"[idx]"索引位置来存放元素，Map中的元素可以直接设置，如果需要访问{@link Map}对象中的成员变量,
     * 需要在变量名前加"#", 如map.#table</p>
     * <p>设置数组与List中的第n个元素：list[n], 高维数组(嵌套集合): list[x][y][z]</p>
     * <p>设置Map中键为key对应的value：map.key</p>
     * <p>设置Map中名称为table的成员变量的值：map.#table</p>
     *
     * @param target    对象实例
     * @param valuePath 需要设置的属性名称，支持嵌套
     * @param value     需要设置的值
     * @param <T>       属性类型参数
     * @return 属性值
     */
    public static <T> T setFieldValue(T target, String valuePath, Object value) {
        String prefixPath;
        String fieldPath;
        boolean isItr = false;

        Object o = target;
        if (Assert.notEmpty(valuePath, "valuePath必须指定属性名称").charAt(valuePath.length() - 1) == ']') {
            int i = valuePath.lastIndexOf('[');
            prefixPath = valuePath.substring(0, Assert.inclusiveBetween(0, valuePath.length() - 2, i, "表达式不合法, '[', ']'没有成对出现"));
            fieldPath = Assert.notEmpty(valuePath.substring(i + 1, valuePath.length() - 1), valuePath + "中包含无效的索引值");
            isItr = true;
        } else {
            int i = valuePath.lastIndexOf('.');
            if (i < 0) {
                prefixPath = null;
                fieldPath = valuePath;
            } else {
                prefixPath = valuePath.substring(0, i);
                fieldPath = valuePath.substring(i + 1);
            }
        }

        if (StringUtils.isNotEmpty(prefixPath)) {
            o = Assert.notNull(getFieldValue(o, prefixPath), "目标对象中的属性" + prefixPath + "为null");
        }

        if (isItr) {
            int idx;
            try {
                idx = Integer.parseInt(fieldPath);
            } catch (NumberFormatException e) {
                throw new SpinException("索引值必须是合法的数字: " + fieldPath);
            }
            if (idx < 0) {
                throw new SpinException("索引不能为负数: " + idx);
            }
            if (o instanceof List) {
                try {
                    //noinspection unchecked
                    ((List) o).set(idx, value);
                } catch (IndexOutOfBoundsException e) {
                    throw new SpinException("为目标对象填充索引为" + idx + "的元素失败: 索引越界");
                } catch (UnsupportedOperationException e) {
                    throw new SpinException("为目标对象填充索引为" + idx + "的元素失败: 集合[" + o.getClass().getName() + "]不支持set操作");
                } catch (ClassCastException e) {
                    throw new SpinException("为目标对象填充索引为" + idx + "的元素失败: 集合[" + o.getClass().getName() + "]类型与元素类型[" + value.getClass().getName() + "]不匹配");
                }
            } else if (o.getClass().isArray()) {
                Object[] objects = (Object[]) o;
                if (objects.length <= idx) {
                    throw new SpinException("为目标对象填充索引为" + idx + "的元素失败: 索引越界");
                }
                try {
                    objects[idx] = o;
                } catch (ClassCastException e) {
                    throw new SpinException("为目标对象填充索引为" + idx + "的元素失败: 集合[" + o.getClass().getName() + "]类型与元素类型[" + value.getClass().getName() + "]不匹配");
                }
            } else {
                throw new SpinException("不支持在类型[" + o.getClass().getName() + "]上进行基于索引的赋值操作");
            }
        } else {
            if (o instanceof Map && fieldPath.charAt(0) != '#') {
                //noinspection unchecked
                ((Map) o).put(fieldPath, value);
            } else {
                Field field = Assert.notNull(ReflectionUtils.findField(o.getClass(), fieldPath), "对象[" + o.getClass().getName() + "]中不存在[" + fieldPath + "字段");
                ReflectionUtils.makeAccessible(field);
                try {
                    field.set(o, value);
                } catch (IllegalAccessException e) {
                    throw new SpinException("为对象[" + o.getClass().getName() + "]设置属性" + fieldPath + "失败, 无法访问该属性", e);
                }
            }
        }

        return target;
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

        throw new SpinException(target.getClass().getName() + "不能转换为Map<String, String>");
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
            throw new SpinException(target.getClass().getName() + "不能被转换为Map");
        }
    }

    /**
     * 判断一个对象是否是JavaBean
     * <p>一个JavaBean，一定是一个自定义对象（非java自带的类，数组，集合，Map，枚举，字符序列，流，异常)</p>
     *
     * @param target 对象
     * @return 是否是JavaBean
     */
    public static boolean isJavaBean(Object target) {
        if (null == target) {
            return false;
        }
        String name = target.getClass().getName();
        return !(target.getClass().isArray()
            || name.startsWith("java.")
            || name.startsWith("javax.")
            || name.startsWith("sun.")
            || name.startsWith("jdk.")
            || name.startsWith("com.sun.")
            || name.startsWith("org.jcp.")
            || name.startsWith("org.omg.")
            || target instanceof Map
            || target instanceof Iterable
            || target instanceof Enum
            || target instanceof CharSequence
            || target instanceof Throwable
            || target instanceof AutoCloseable
            || target instanceof Readable
        );
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
                    props.put(descriptor.getName(), new PropertyDescriptorWrapper(descriptor, descriptor.getReadMethod(), writer));
            }
            CLASS_PROPERTY_CACHE.put(type.getName(), props);
        }
        return props;
    }

    /**
     * 通过内省机制，获取一个JavaBean的所有属性(属性：一个类的成员变量，并且Getter与Setter至少存在一个)
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
                    props.put(descriptor.getName(), new PropertyDescriptorWrapper(descriptor, reader, writer));
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

        public void applyProperty(Object target, Object propVal) {
            try {
                writer.invoke(target, ObjectUtils.convert(protertyType, propVal));
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.warn("Java Bean - {}设置属性[{}]失败", target.getClass().getName(), descriptor.getName());
                if (logger.isDebugEnabled()) {
                    logger.debug("错误详情: ", e);
                }
            }
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
     * <p>如果不指定字段列表，并且源对象是JavaBean，则会拷贝所有字段。如果类型不匹配，会尝试进行类型转换</p>
     *
     * @param src    源实体
     * @param target 目标实体
     * @param fields 需要拷贝的字段名(如果为空则表示拷贝所有共有字段)
     * @param ignore 需要排除的字段名
     */
    public static void copyTo(Object src, Object target, Collection<String> fields, Collection<String> ignore) {
        if (null == src || null == target) {
            return;
        }

        Map<String, Field> srcProps = ReflectionUtils.getAllDeclaredField(src.getClass());
        Map<String, Field> targetProps = ReflectionUtils.getAllDeclaredField(target.getClass());
        Set<String> ignoreFields = CollectionUtils.isEmpty(ignore) ? Collections.emptySet() : new HashSet<>(ignore);

        if (null == fields || fields.isEmpty()) {
            if (isJavaBean(src)) {
                fields = targetProps.keySet().stream()
                    .filter(srcProps::containsKey)
                    .filter(it -> !ignoreFields.contains(it))
                    .collect(Collectors.toList());
            } else {
                throw new SpinException("非JavaBean请指定需要Copy的属性列表");
            }
        }

        try {
            for (String field : fields) {
                Field targetField = targetProps.get(field);
                Field srcField = srcProps.get(field);

                if (null == srcField) {
                    throw new SpinException("属性" + field + "在源对象中不存在");
                }

                if (null == targetField) {
                    throw new SpinException("属性" + field + "在目标对象中不存在");
                }
                targetField.set(target, ObjectUtils.convert(targetField.getType(), srcField.get(src)));
            }
        } catch (IllegalAccessException e) {
            throw new SpinException("反射访问成员变量失败", e);
        }
    }

    /**
     * 复制JavaBean的属性到另一个JavaBean中，直接反射字段值，不通过getter/setter
     * <p>如果不指定字段列表，并且源对象是JavaBean，则会拷贝所有字段</p>
     *
     * @param src    源实体
     * @param target 目标实体
     * @param fields 字段名列表
     */
    public static void copyTo(Object src, Object target, String... fields) {
        copyTo(src, target, Arrays.asList(fields), null);
    }

    public static <S, T> void copyTo(S src, T target, Function<S, ?> prop) {
        copyTo(src, target, Collections.singletonList(
            BeanUtils.toFieldName(LambdaUtils.resolveLambda(prop).getImplMethodName())
        ), null);
    }

    public static <S, T> void copyTo(S src, T target, Supplier<?> prop) {
        copyTo(src, target, Collections.singletonList(
            BeanUtils.toFieldName(LambdaUtils.resolveLambda(prop).getImplMethodName())
        ), null);
    }

    public static <S, T> void copyTo(S src, T target, Function<S, ?> prop1, Function<S, ?> prop2) {
        copyTo(src, target, Arrays.asList(
            BeanUtils.toFieldName(LambdaUtils.resolveLambda(prop1).getImplMethodName()),
            BeanUtils.toFieldName(LambdaUtils.resolveLambda(prop2).getImplMethodName())
        ), null);
    }

    public static <S, T> void copyTo(S src, T target, Supplier<?> prop1, Supplier<?> prop2) {
        copyTo(src, target, Arrays.asList(
            BeanUtils.toFieldName(LambdaUtils.resolveLambda(prop1).getImplMethodName()),
            BeanUtils.toFieldName(LambdaUtils.resolveLambda(prop2).getImplMethodName())
        ), null);
    }

    public static <S, T> void copyTo(S src, T target, Function<S, ?> prop1, Function<S, ?> prop2, Function<S, ?> prop3) {
        copyTo(src, target, Arrays.asList(
            BeanUtils.toFieldName(LambdaUtils.resolveLambda(prop1).getImplMethodName()),
            BeanUtils.toFieldName(LambdaUtils.resolveLambda(prop2).getImplMethodName()),
            BeanUtils.toFieldName(LambdaUtils.resolveLambda(prop3).getImplMethodName())
        ), null);
    }

    public static <S, T> void copyTo(S src, T target, Supplier<?> prop1, Supplier<?> prop2, Supplier<?> prop3) {
        copyTo(src, target, Arrays.asList(
            BeanUtils.toFieldName(LambdaUtils.resolveLambda(prop1).getImplMethodName()),
            BeanUtils.toFieldName(LambdaUtils.resolveLambda(prop2).getImplMethodName()),
            BeanUtils.toFieldName(LambdaUtils.resolveLambda(prop3).getImplMethodName())
        ), null);
    }

    public static <S, T> void copyTo(S src, T target,
                                     Function<S, ?> prop1,
                                     Function<S, ?> prop2,
                                     Function<S, ?> prop3,
                                     Function<S, ?> prop4) {
        copyTo(src, target, LambdaUtils.resolveLambda(prop1), LambdaUtils.resolveLambda(prop2), LambdaUtils.resolveLambda(prop3),
            LambdaUtils.resolveLambda(prop4));
    }

    public static <S, T> void copyTo(S src, T target,
                                     Supplier<?> prop1,
                                     Supplier<?> prop2,
                                     Supplier<?> prop3,
                                     Supplier<?> prop4) {
        copyTo(src, target, LambdaUtils.resolveLambda(prop1), LambdaUtils.resolveLambda(prop2), LambdaUtils.resolveLambda(prop3),
            LambdaUtils.resolveLambda(prop4));
    }

    public static <S, T> void copyTo(S src, T target,
                                     Function<S, ?> prop1,
                                     Function<S, ?> prop2,
                                     Function<S, ?> prop3,
                                     Function<S, ?> prop4,
                                     Function<S, ?> prop5) {
        copyTo(src, target, LambdaUtils.resolveLambda(prop1), LambdaUtils.resolveLambda(prop2), LambdaUtils.resolveLambda(prop3),
            LambdaUtils.resolveLambda(prop4), LambdaUtils.resolveLambda(prop5));
    }

    public static <S, T> void copyTo(S src, T target,
                                     Supplier<?> prop1,
                                     Supplier<?> prop2,
                                     Supplier<?> prop3,
                                     Supplier<?> prop4,
                                     Supplier<?> prop5) {
        copyTo(src, target, LambdaUtils.resolveLambda(prop1), LambdaUtils.resolveLambda(prop2), LambdaUtils.resolveLambda(prop3),
            LambdaUtils.resolveLambda(prop4), LambdaUtils.resolveLambda(prop5));
    }

    public static <S, T>
    void copyTo(S src, T target,
                Function<S, ?> prop1,
                Function<S, ?> prop2,
                Function<S, ?> prop3,
                Function<S, ?> prop4,
                Function<S, ?> prop5,
                Function<S, ?> prop6) {
        copyTo(src, target, LambdaUtils.resolveLambda(prop1), LambdaUtils.resolveLambda(prop2), LambdaUtils.resolveLambda(prop3),
            LambdaUtils.resolveLambda(prop4), LambdaUtils.resolveLambda(prop5), LambdaUtils.resolveLambda(prop6));
    }

    public static <S, T> void copyTo(S src, T target,
                                     Supplier<?> prop1,
                                     Supplier<?> prop2,
                                     Supplier<?> prop3,
                                     Supplier<?> prop4,
                                     Supplier<?> prop5,
                                     Supplier<?> prop6) {
        copyTo(src, target, LambdaUtils.resolveLambda(prop1), LambdaUtils.resolveLambda(prop2), LambdaUtils.resolveLambda(prop3),
            LambdaUtils.resolveLambda(prop4), LambdaUtils.resolveLambda(prop5), LambdaUtils.resolveLambda(prop6));
    }

    public static <S, T>
    void copyTo(S src, T target,
                Function<S, ?> prop1,
                Function<S, ?> prop2,
                Function<S, ?> prop3,
                Function<S, ?> prop4,
                Function<S, ?> prop5,
                Function<S, ?> prop6,
                Function<S, ?> prop7) {
        copyTo(src, target, LambdaUtils.resolveLambda(prop1), LambdaUtils.resolveLambda(prop2), LambdaUtils.resolveLambda(prop3),
            LambdaUtils.resolveLambda(prop4), LambdaUtils.resolveLambda(prop5), LambdaUtils.resolveLambda(prop6),
            LambdaUtils.resolveLambda(prop7));
    }

    public static <S, T> void copyTo(S src, T target,
                                     Supplier<?> prop1,
                                     Supplier<?> prop2,
                                     Supplier<?> prop3,
                                     Supplier<?> prop4,
                                     Supplier<?> prop5,
                                     Supplier<?> prop6,
                                     Supplier<?> prop7) {
        copyTo(src, target, LambdaUtils.resolveLambda(prop1), LambdaUtils.resolveLambda(prop2), LambdaUtils.resolveLambda(prop3),
            LambdaUtils.resolveLambda(prop4), LambdaUtils.resolveLambda(prop5), LambdaUtils.resolveLambda(prop6),
            LambdaUtils.resolveLambda(prop7));
    }

    public static <S, T> void copyTo(S src, T target,
                                     Function<S, ?> prop1,
                                     Function<S, ?> prop2,
                                     Function<S, ?> prop3,
                                     Function<S, ?> prop4,
                                     Function<S, ?> prop5,
                                     Function<S, ?> prop6,
                                     Function<S, ?> prop7,
                                     Function<S, ?> prop8) {
        copyTo(src, target, LambdaUtils.resolveLambda(prop1), LambdaUtils.resolveLambda(prop2), LambdaUtils.resolveLambda(prop3),
            LambdaUtils.resolveLambda(prop4), LambdaUtils.resolveLambda(prop5), LambdaUtils.resolveLambda(prop6),
            LambdaUtils.resolveLambda(prop7), LambdaUtils.resolveLambda(prop8));
    }

    public static <S, T> void copyTo(S src, T target,
                                     Supplier<?> prop1,
                                     Supplier<?> prop2,
                                     Supplier<?> prop3,
                                     Supplier<?> prop4,
                                     Supplier<?> prop5,
                                     Supplier<?> prop6,
                                     Supplier<?> prop7,
                                     Supplier<?> prop8) {
        copyTo(src, target, LambdaUtils.resolveLambda(prop1), LambdaUtils.resolveLambda(prop2), LambdaUtils.resolveLambda(prop3),
            LambdaUtils.resolveLambda(prop4), LambdaUtils.resolveLambda(prop5), LambdaUtils.resolveLambda(prop6),
            LambdaUtils.resolveLambda(prop7), LambdaUtils.resolveLambda(prop8));
    }

    /**
     * 复制JavaBean的属性到另一个JavaBean中，直接反射字段值，不通过getter/setter
     * <p>如果不指定字段列表，并且源对象是JavaBean，则会拷贝所有字段</p>
     *
     * @param src    源实体
     * @param target 目标实体
     * @param ignore 忽略的字段名列表
     */
    public static void copyToIgnore(Object src, Object target, String... ignore) {
        copyTo(src, target, null, Arrays.asList(ignore));
    }

    public static <S, T> void copyToIgnore(S src, T target, Function<S, ?> prop) {
        copyTo(src, target, null, Collections.singletonList(
            BeanUtils.toFieldName(LambdaUtils.resolveLambda(prop).getImplMethodName())
        ));
    }

    public static <S, T> void copyToIgnore(S src, T target, Supplier<?> prop) {
        copyTo(src, target, null, Collections.singletonList(
            BeanUtils.toFieldName(LambdaUtils.resolveLambda(prop).getImplMethodName())
        ));
    }

    public static <S, T> void copyToIgnore(S src, T target, Function<S, ?> prop1, Function<S, ?> prop2) {
        copyTo(src, target, null, Arrays.asList(
            BeanUtils.toFieldName(LambdaUtils.resolveLambda(prop1).getImplMethodName()),
            BeanUtils.toFieldName(LambdaUtils.resolveLambda(prop2).getImplMethodName())
        ));
    }

    public static <S, T> void copyToIgnore(S src, T target, Supplier<?> prop1, Supplier<?> prop2) {
        copyTo(src, target, null, Arrays.asList(
            BeanUtils.toFieldName(LambdaUtils.resolveLambda(prop1).getImplMethodName()),
            BeanUtils.toFieldName(LambdaUtils.resolveLambda(prop2).getImplMethodName())
        ));
    }

    public static <S, T> void copyToIgnore(S src, T target, Function<S, ?> prop1, Function<S, ?> prop2, Function<S, ?> prop3) {
        copyTo(src, target, null, Arrays.asList(
            BeanUtils.toFieldName(LambdaUtils.resolveLambda(prop1).getImplMethodName()),
            BeanUtils.toFieldName(LambdaUtils.resolveLambda(prop2).getImplMethodName()),
            BeanUtils.toFieldName(LambdaUtils.resolveLambda(prop3).getImplMethodName())
        ));
    }

    public static <S, T> void copyToIgnore(S src, T target, Supplier<?> prop1, Supplier<?> prop2, Supplier<?> prop3) {
        copyTo(src, target, null, Arrays.asList(
            BeanUtils.toFieldName(LambdaUtils.resolveLambda(prop1).getImplMethodName()),
            BeanUtils.toFieldName(LambdaUtils.resolveLambda(prop2).getImplMethodName()),
            BeanUtils.toFieldName(LambdaUtils.resolveLambda(prop3).getImplMethodName())
        ));
    }

    public static <S, T> void copyToIgnore(S src, T target,
                                           Function<S, ?> prop1,
                                           Function<S, ?> prop2,
                                           Function<S, ?> prop3,
                                           Function<S, ?> prop4) {
        copyToIgnore(src, target, LambdaUtils.resolveLambda(prop1), LambdaUtils.resolveLambda(prop2), LambdaUtils.resolveLambda(prop3),
            LambdaUtils.resolveLambda(prop4));
    }

    public static <S, T> void copyToIgnore(S src, T target,
                                           Supplier<?> prop1,
                                           Supplier<?> prop2,
                                           Supplier<?> prop3,
                                           Supplier<?> prop4) {
        copyToIgnore(src, target, LambdaUtils.resolveLambda(prop1), LambdaUtils.resolveLambda(prop2), LambdaUtils.resolveLambda(prop3),
            LambdaUtils.resolveLambda(prop4));
    }

    /**
     * 将Map转换为对象
     * <p>
     * 复合属性，请在语句中指定别名为实体属性的路径。如createUser.id对应createUser的id属性。<br>
     * 如果Map中存在某些Key不能与实体的属性对应，将被舍弃。
     * </p>
     *
     * @param type   对象类型
     * @param values map
     * @param <T>    类型参数
     * @return 转换后的对象
     * @throws IllegalAccessException    对象访问异常
     * @throws InstantiationException    对象初始化异常
     * @throws InvocationTargetException 方法调用异常
     * @throws NoSuchMethodException     方法不存在
     */
    public static <T> T wrapperMapToBean(Class<T> type, Map<String, Object> values) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        T bean = type.getDeclaredConstructor().newInstance();
        Map<String, BeanUtils.PropertyDescriptorWrapper> props = BeanUtils.getBeanPropertyDes(type);
        if (props.size() == 0)
            return bean;
        int off;
        int next;
        int depth;
        String p;
        String[] propName = new String[100];
        Object[] args = new Object[1];
        Map<String, BeanUtils.PropertyDescriptorWrapper> workerProps;
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
                BeanUtils.PropertyDescriptorWrapper prop = props.get(p);
                if (null == prop)
                    continue;
                args[0] = ObjectUtils.convert(prop.protertyType, entry.getValue());
                prop.writer.invoke(bean, args);
                continue;
            }
            int i = 0;
            Object worker = bean;
            Class<?> propType;
            workerProps = BeanUtils.getBeanPropertyDes(type);
            while (depth != i) {
                BeanUtils.PropertyDescriptorWrapper prop = workerProps.get(propName[i]);
                if (null == prop) {
                    ++i;
                    continue;
                }
                propType = prop.protertyType;
                if (i != depth - 1) {
                    Object ib = prop.reader == null ? null : prop.reader.invoke(worker);
                    if (null == ib) {
                        ib = propType.getDeclaredConstructor().newInstance();
                        args[0] = ObjectUtils.convert(propType, ib);
                        prop.writer.invoke(worker, args);
                    }
                    workerProps = BeanUtils.getBeanPropertyDes(propType);
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
     *
     * @param type       对象类型
     * @param values     map
     * @param propPrefix 属性前缀
     * @param <T>        类型参数
     * @return 转换后的对象
     * @throws IllegalAccessException    对象访问异常
     * @throws InstantiationException    对象初始化异常
     * @throws InvocationTargetException 方法调用异常
     */
    @Deprecated
    public static <T> T wrapperMapToBean(Class<T> type, Map<String, Object> values, String propPrefix) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        T bean = type.newInstance();
        Map<String, BeanUtils.PropertyDescriptorWrapper> props = BeanUtils.getBeanPropertyDes(type);

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (StringUtils.isNotEmpty(propPrefix) && !entry.getKey().toLowerCase().startsWith(propPrefix))
                continue;
            String propName = StringUtils.isEmpty(propPrefix) ? entry.getKey().toLowerCase() : entry.getKey().substring(propPrefix.length() + 1).toLowerCase();
            int index = propName.indexOf('.');
            if (index > 0) {
                propName = propName.substring(0, index);
                if (props.containsKey(propName)) {
                    BeanUtils.PropertyDescriptorWrapper prop = props.get(propName);
                    Object tmp = wrapperMapToBean(prop.protertyType, values, StringUtils.isEmpty(propPrefix) ? propName : propPrefix + "." + propName);
                    Object[] args = new Object[1];
                    args[0] = tmp;
                    prop.writer.invoke(bean, args);
                }
            } else {
                if (props.containsKey(propName)) {
                    BeanUtils.PropertyDescriptorWrapper prop = props.get(propName);
                    Object[] args = new Object[1];
                    args[0] = ObjectUtils.convert(prop.protertyType, entry.getValue());
                    prop.writer.invoke(bean, args);
                }
            }
        }
        return bean;
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
            throw new SpinException("解析Bean属性异常", e);
        }
        return beanInfo.getPropertyDescriptors();
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

    private static <S, T> void copyTo(S src, T target, SerializedLambda... serializedLambdas) {
        copyTo(src, target, Arrays.stream(serializedLambdas)
            .map(SerializedLambda::getImplMethodName)
            .map(BeanUtils::toFieldName)
            .collect(Collectors.toList()), null);
    }

    private static <S, T> void copyToIgnore(S src, T target, SerializedLambda... serializedLambdas) {
        copyTo(src, target, null, Arrays.stream(serializedLambdas)
            .map(SerializedLambda::getImplMethodName)
            .map(BeanUtils::toFieldName)
            .collect(Collectors.toList()));
    }
}
