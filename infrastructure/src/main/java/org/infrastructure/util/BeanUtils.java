/*
 *  Copyright 2002-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.infrastructure.util;

import org.infrastructure.jpa.core.IEntity;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean工具类
 * Created by xuweinan on 2016/8/15.
 */
public class BeanUtils {
    public static final Map<String, Map<String, PropertyDescriptor>> CLASS_PROPERTY_CACHE = new ConcurrentHashMap<>();

    public static PropertyDescriptor[] propertyDescriptors(Class<?> c) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(c);
        return beanInfo.getPropertyDescriptors();
    }

    public static List<Method> getterMethod(Class<?> c) {
        PropertyDescriptor[] ps;
        try {
            ps = propertyDescriptors(c);
        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }
        List<Method> list = new ArrayList<>();
        for (PropertyDescriptor p : ps) {
            if (p.getReadMethod() != null && p.getWriteMethod() != null) {
                list.add(p.getReadMethod());
            }
        }
        return list;
    }

    public static Method tailMethod(Class<?> type, String name) {
        try {
            return type.getMethod(name, String.class, Object.class);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (SecurityException e) {
            return null;
        }
    }

    /**
     * 将Map转换为对象
     * <p>
     * 复合属性，请在语句中指定别名为实体属性的路径。如createUser.id对应createUser的id属性。<br>
     * 如果Map中存在某些Key不能与实体的属性对应，将被舍弃。
     * </p>
     * 由于递归实现效率低下，不建议使用
     */
    public static <T> T wrapperMapToBean(Class<T> type, Map<String, Object> values, String propPrefix) throws IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException {
        T bean = type.newInstance();
        Map<String, PropertyDescriptor> props = CLASS_PROPERTY_CACHE.get(type.getName());
        if (null == props) {
            PropertyDescriptor[] propertyDescriptors = propertyDescriptors(type);
            if (null == propertyDescriptors || 0 == propertyDescriptors.length)
                return type.newInstance();
            props = new HashMap<>();
            for (PropertyDescriptor descriptor : propertyDescriptors) {
                props.put(descriptor.getName().toLowerCase(), descriptor);
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
                    PropertyDescriptor prop = props.get(propName);
                    Object tmp = wrapperMapToBean(prop.getPropertyType(), values, StringUtils.isEmpty(propPrefix) ? propName : propPrefix + "." + propName);
                    Object[] args = new Object[1];
                    args[0] = tmp;
                    Method writer = prop.getWriteMethod();
                    if (null != writer) writer.invoke(bean, args);
                }
            } else {
                if (props.containsKey(propName)) {
                    PropertyDescriptor prop = props.get(propName);
                    Object[] args = new Object[1];
                    args[0] = ObjectUtils.convert(prop.getPropertyType(), entry.getValue());
                    Method writer = prop.getWriteMethod();
                    if (null != writer) writer.invoke(bean, args);
                }
            }
        }
        return bean;
    }

    public static Map<String, PropertyDescriptor> getBeanPropertyDes(Class<?> type) throws IntrospectionException {
        Map<String, PropertyDescriptor> props = CLASS_PROPERTY_CACHE.get(type.getName());
        if (null == props) {
            PropertyDescriptor[] propertyDescriptors = propertyDescriptors(type);
            props = new HashMap<>();
            for (PropertyDescriptor descriptor : propertyDescriptors) {
                props.put(descriptor.getName().toLowerCase(), descriptor);
            }
            CLASS_PROPERTY_CACHE.put(type.getName(), props);
        }
        return props;
    }

    /**
     * 将Map转换为对象
     * <p>
     * 复合属性，请在语句中指定别名为实体属性的路径。如createUser.id对应createUser的id属性。<br>
     * 如果Map中存在某些Key不能与实体的属性对应，将被舍弃。
     * </p>
     * 性能与gson基本持平
     */
    public static <T> T wrapperMapToBean(Class<T> type, Map<String, Object> values) throws IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException {
        T bean = type.newInstance();
        Map<String, PropertyDescriptor> props = getBeanPropertyDes(type);
        if (props.size() == 0)
            return bean;
        int off;
        int next;
        int depth;
        String p;
        String[] propName = new String[100];
        Object[] args = new Object[1];
        Map<String, PropertyDescriptor> workerProps;
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
                PropertyDescriptor prop = props.get(p);
                if (null == prop)
                    continue;
                args[0] = ObjectUtils.convert(prop.getPropertyType(), entry.getValue());
                Method writer = prop.getWriteMethod();
                if (null != writer) writer.invoke(bean, args);
                continue;
            }
            int i = 0;
            Object worker = bean;
            Class<?> propType;
            workerProps = getBeanPropertyDes(type);
            while (depth != i) {
                PropertyDescriptor prop = workerProps.get(propName[i]);
                if (null == prop) {
                    ++i;
                    continue;
                }
                propType = prop.getPropertyType();
                if (i != depth - 1 && IEntity.class.isAssignableFrom(propType)) {
                    Object ib = prop.getReadMethod().invoke(worker);
                    if (null == ib)
                        ib = propType.newInstance();
                    args[0] = ObjectUtils.convert(propType, ib);
                    Method writer = prop.getWriteMethod();
                    if (null != writer) writer.invoke(worker, args);
                    workerProps = getBeanPropertyDes(propType);
                    worker = ib;
                    ++i;
                    continue;
                }
                if (i == depth - 1) {
                    args[0] = ObjectUtils.convert(propType, entry.getValue());
                    Method writer = prop.getWriteMethod();
                    if (null != writer) writer.invoke(worker, args);
                }
                ++i;
            }
        }
        return bean;
    }

    /**
     * 将平面的Map转换成树状组织的Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> wrapperFlatMap(Map<String, Object> values) throws IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException {
        Map<String, Object> propValue = new HashMap<>();
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
            p = entry.getKey().toLowerCase();
            while ((next = p.indexOf('.', off)) != -1) {
                propName[depth++] = p.substring(off, next);
                off = next + 1;
            }
            propName[depth++] = (p.substring(off));
            if (depth == 1) {
                propValue.put(propName[0], entry.getValue());
                continue;
            }
            work = propValue;
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
                    continue;
                }
                work.put(propName[i], entry.getValue());
                ++i;
            }
        }
        return propValue;
    }
}