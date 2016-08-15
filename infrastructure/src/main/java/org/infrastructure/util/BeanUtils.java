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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean工具类
 * Created by xuweinan on 2016/8/15.
 */
public class BeanUtils {

    public static PropertyDescriptor[] propertyDescriptors(Class<?> c) throws IntrospectionException {
        BeanInfo beanInfo = null;
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
}