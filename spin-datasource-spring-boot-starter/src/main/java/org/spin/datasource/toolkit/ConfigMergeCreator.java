package org.spin.datasource.toolkit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

public class ConfigMergeCreator<C, T> {
    private static final Logger logger = LoggerFactory.getLogger(ConfigMergeCreator.class);
    private final String configName;

    private final Class<C> configClazz;

    private final Class<T> targetClazz;

    public ConfigMergeCreator(String configName, Class<C> configClazz, Class<T> targetClazz) {
        this.configName = configName;
        this.configClazz = configClazz;
        this.targetClazz = targetClazz;
    }

    public T create(C global, C item) {
        try {
            T result = targetClazz.newInstance();
            BeanInfo beanInfo = Introspector.getBeanInfo(configClazz, Object.class);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor pd : propertyDescriptors) {
                Class<?> propertyType = pd.getPropertyType();
                if (Properties.class == propertyType) {
                    mergeProperties(global, item, result, pd);
                } else {
                    mergeBasic(global, item, result, pd);
                }
            }
            return result;
        } catch (IntrospectionException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void mergeProperties(C global, C item, T result, PropertyDescriptor pd) {
        String name = pd.getName();
        Method readMethod = pd.getReadMethod();
        try {
            Properties itemValue = (Properties) readMethod.invoke(item);
            Properties globalValue = (Properties) readMethod.invoke(global);
            Properties properties = new Properties();
            if (globalValue != null) {
                properties.putAll(globalValue);
            }
            if (itemValue != null) {
                properties.putAll(itemValue);
            }
            if (properties.size() > 0) {
                setField(result, name, properties);
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void mergeBasic(C global, C item, T result, PropertyDescriptor pd) {
        String name = pd.getName();
        Method readMethod = pd.getReadMethod();

        try {
            Object value = readMethod.invoke(item);
            if (value == null) {
                value = readMethod.invoke(global);
            }
            if (value != null) {
                setField(result, name, value);
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(T result, String name, Object value) {
        try {
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(name, targetClazz);
            Method writeMethod = propertyDescriptor.getWriteMethod();
            writeMethod.invoke(result, value);
        } catch (IntrospectionException | ReflectiveOperationException e) {
            Field field = null;
            try {
                field = targetClazz.getDeclaredField(name);
                field.setAccessible(true);
                field.set(result, value);
            } catch (ReflectiveOperationException e1) {
                logger.warn("dynamic-datasource set {} [{}] failed,please check your config or update {}  to the latest version", configName, name, configName);
            } finally {
                if (field != null && field.isAccessible()) {
                    field.setAccessible(false);
                }
            }
        } catch (Exception ee) {
            logger.warn("dynamic-datasource set {} [{}] failed,please check your config", configName, name, ee);
        }
    }

}
