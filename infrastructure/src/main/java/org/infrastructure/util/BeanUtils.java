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
import org.infrastructure.sys.EnvCache;
import org.infrastructure.throwable.SimplifiedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Bean工具类
 * Created by xuweinan on 2016/8/15.
 */
public abstract class BeanUtils {
    private static final Logger logger = LoggerFactory.getLogger(BeanUtils.class);

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

    public static Method tailMethod(Class<?> type, String name) {
        try {
            return type.getMethod(name, String.class, Object.class);
        } catch (NoSuchMethodException | SecurityException e) {
            logger.error(e.getMessage());
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
    @Deprecated
    public static <T> T wrapperMapToBean(Class<T> type, Map<String, Object> values, String propPrefix) throws IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException {
        T bean = type.newInstance();
        Map<String, PropertyDescriptorWrapper> props = EnvCache.CLASS_PROPERTY_CACHE.get(type.getName());
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
            EnvCache.CLASS_PROPERTY_CACHE.put(type.getName(), props);
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
        Map<String, PropertyDescriptorWrapper> props = EnvCache.CLASS_PROPERTY_CACHE.get(type.getName());
        if (null == props) {
            PropertyDescriptor[] propertyDescriptors = propertyDescriptors(type);
            props = new HashMap<>();
            Method writer;
            for (PropertyDescriptor descriptor : propertyDescriptors) {
                writer = descriptor.getWriteMethod();
                if (writer != null)
                    props.put(descriptor.getName().toLowerCase(), new PropertyDescriptorWrapper(descriptor, writer));
            }
            EnvCache.CLASS_PROPERTY_CACHE.put(type.getName(), props);
        }
        return props;
    }

    /**
     * 将Map转换为对象
     * <p>
     * 复合属性，请在语句中指定别名为实体属性的路径。如createUser.id对应createUser的id属性。<br>
     * 如果Map中存在某些Key不能与实体的属性对应，将被舍弃。
     * </p>
     */
    public static <T> T wrapperMapToBean(Class<T> type, Map<String, Object> values) throws IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException {
        T bean = type.newInstance();
        Map<String, PropertyDescriptorWrapper> props = EnvCache.CLASS_PROPERTY_CACHE.get(type.getName());
        if (null == props) {
            PropertyDescriptor[] propertyDescriptors = propertyDescriptors(type);
            props = new HashMap<>();
            Method writer;
            for (PropertyDescriptor descriptor : propertyDescriptors) {
                writer = descriptor.getWriteMethod();
                if (writer != null)
                    props.put(descriptor.getName().toLowerCase(), new PropertyDescriptorWrapper(descriptor, writer));
            }
            EnvCache.CLASS_PROPERTY_CACHE.put(type.getName(), props);
        }
        if (props.size() == 0)
            return bean;
        int off;
        int next;
        int depth;
        String p;
        String[] propName = new String[100];
        Object[] args = new Object[1];
        Map<String, PropertyDescriptorWrapper> workerProps;
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
                PropertyDescriptorWrapper prop = props.get(p);
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
                PropertyDescriptorWrapper prop = workerProps.get(propName[i]);
                if (null == prop) {
                    ++i;
                    continue;
                }
                propType = prop.protertyType;
                if (i != depth - 1 && IEntity.class.isAssignableFrom(propType)) {
                    Object ib = prop.reader == null ? null : prop.reader.invoke(worker);
                    if (null == ib) {
                        ib = propType.newInstance();
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
     * 将平面的Map转换成树状组织的Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> wrapperFlatMap(Map<String, Object> values) {
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
            p = entry.getKey();
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