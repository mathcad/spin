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

/**
 * Bean工具类
 * Created by xuweinan on 2016/8/15.
 */
public class BeanUtils {

    public static PropertyDescriptor[] propertyDescriptors(Class<?> c) throws IntrospectionException {
        BeanInfo beanInfo;
        beanInfo = Introspector.getBeanInfo(c);
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
     */
    public static <T> T wrapperMapToBean(Class<T> type, Map<String, Object> values, String propPrefix) throws IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException {
        T bean = type.newInstance();
        PropertyDescriptor[] propertyDescriptors = BeanUtils.propertyDescriptors(type);
        if (null == propertyDescriptors || 0 == propertyDescriptors.length)
            return bean;
        Map<String, PropertyDescriptor> props = new HashMap<>();
        for (PropertyDescriptor descriptor : propertyDescriptors) {
            props.put(descriptor.getName().toLowerCase(), descriptor);
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

    public static <T> T wrapperMapToBean(Class<T> type, Map<String, Object> values) throws IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException {
        T bean = type.newInstance();
        PropertyDescriptor[] propertyDescriptors = BeanUtils.propertyDescriptors(type);
        if (null == propertyDescriptors || 0 == propertyDescriptors.length)
            return bean;
        Map<String, PropertyDescriptor> props = new HashMap<>();
        for (PropertyDescriptor descriptor : propertyDescriptors) {
            props.put(descriptor.getName().toLowerCase(), descriptor);
        }

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String[] propName = entry.getKey().replaceAll("_", "").toLowerCase().split("\\.");
            int depth = propName.length;
            if (depth != 1) {
                while (depth != 0) {

                }
            } else {
                if (props.containsKey(propName[1])) {
                    PropertyDescriptor prop = props.get(propName[1]);
                    Object[] args = new Object[1];
                    args[0] = ObjectUtils.convert(prop.getPropertyType(), entry.getValue());
                    Method writer = prop.getWriteMethod();
                    if (null != writer) writer.invoke(bean, args);
                }
            }
        }
        return bean;
    }
}