package org.spin.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;
import org.spin.core.throwable.SimplifiedException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Bean工具类
 * Created by xuweinan on 2016/8/15.
 */
public abstract class BeanUtils {
    private static final Logger logger = LoggerFactory.getLogger(BeanUtils.class);

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
            if (i < valuePath.length() - 1 && null == o) {
                throw new SimplifiedException(field + "属性为null");
            }
            if (field.charAt(0) == '#' && o instanceof Map) {
                o = ((Map) o).get(field.substring(1));
            } else if (field.charAt(0) == '#' && o instanceof List) {
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
            } else if (field.charAt(0) == '#' && o.getClass().isArray()) {
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
}
