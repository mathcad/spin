package org.spin.cloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.cloud.annotation.UtilClass;
import org.spin.core.util.ReflectionUtils;
import org.spin.core.util.Util;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;

import java.lang.reflect.Modifier;

/**
 * 工具类初始化器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/9/3</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class UtilsClassInitApplicationRunner implements ApplicationRunner, ApplicationContextAware, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(UtilsClassInitApplicationRunner.class);

    private ApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) {
        Util.setInitiator(this::procClass);
    }

    private void procClass(Class<?> clazz) {
        UtilClass annotation = clazz.getAnnotation(UtilClass.class);
        String initMethod = null == annotation ? "init" : annotation.initMethod();
        ReflectionUtils.doWithMethods(clazz, method -> {
            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] methodArgs = new Object[parameterTypes.length];
            for (int i = 0; i < methodArgs.length; i++) {
                if (BeanFactory.class.isAssignableFrom(parameterTypes[i])) {
                    methodArgs[i] = applicationContext;
                } else {
                    try {
                        methodArgs[i] = applicationContext.getBean(parameterTypes[i]);
                    } catch (BeansException e) {
                        logger.error("工具类" + clazz.getName() + "的初始化方法" + method.getName() + "注入[" + parameterTypes[i].getName() + "]参数错误", e);
                        return;
                    }
                }
            }
            ReflectionUtils.makeAccessible(method);
            try {
                method.invoke(null, methodArgs);
            } catch (Exception e) {
                logger.error("工具类[{}]的初始化方法[{}]执行异常: {}", clazz, initMethod, e.getMessage());
            }
        }, method -> Modifier.isStatic(method.getModifiers()) && method.getName().equals(initMethod));
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
