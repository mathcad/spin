package org.spin.boot.rest;

import org.spin.core.SpinContext;
import org.spin.core.inspection.MethodDescriptor;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.MethodUtils;
import org.spin.core.util.ReflectionUtils;
import org.spin.core.util.StringUtils;
import org.spin.data.extend.SpringJtaPlatformAdapter;
import org.spin.web.annotation.RestfulInterface;
import org.spin.web.annotation.RestfulMethod;
import org.spin.web.annotation.RestfulService;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.jta.JtaTransactionManager;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Bean后处理器
 * <p>Created by xuweinan on 2017/1/23.</p>
 */
@Component
public class SpinBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {

        // 处理JTA事务管理器
        if (bean instanceof JtaTransactionManager) {
            SpringJtaPlatformAdapter.setJtaTransactionManager((JtaTransactionManager) bean);
        }

        // 处理Rest服务Bean
        processRestService(bean, beanName);

        return bean;
    }

    /**
     * 处理Rest服务Bean
     *
     * @param bean     bean对象
     * @param beanName bean名称
     */
    private void processRestService(Object bean, String beanName) {
        Class<?> beanClass = bean.getClass();

        RestfulService annoService = Optional.ofNullable(beanClass.getAnnotation(RestfulService.class)).orElse(null);
        if (null != annoService) {
            String value = annoService.value();
            ReflectionUtils.doWithMethods(beanClass, method -> processRestMethod(bean, StringUtils.isEmpty(value) ? beanName : value, method));
        }

        Arrays.stream(beanClass.getInterfaces()).filter(i -> Objects.nonNull(i.getAnnotation(RestfulInterface.class))).forEach(i -> {
            RestfulInterface annotation = i.getAnnotation(RestfulInterface.class);
            String value = annotation.value();
            ReflectionUtils.doWithMethods(i, method -> processRestMethod(bean, StringUtils.isEmpty(value) ? beanName : value, method));
        });
    }

    private void processRestMethod(final Object bean, final String module, final Method method) {
        Optional.ofNullable(method.getAnnotation(RestfulMethod.class)).ifPresent((RestfulMethod anno) -> {
            final String service = StringUtils.isEmpty(anno.value()) ? method.getName() : anno.value();
            if (MethodUtils.containsGenericArg(method)) {
                throw new SimplifiedException("RestfulMethod注解的方法不能有泛型参数: " + method.getName() + "@" + bean.getClass());
            }
            List<MethodDescriptor> restMethod = SpinContext.getRestMethod(module, service);
            if (CollectionUtils.isEmpty(restMethod) || restMethod.stream().noneMatch(d -> method.toString().equals(d.getMethod().toString()))) {
                MethodDescriptor descriptor = new MethodDescriptor(method);
                SpinContext.addRestMethod(module, service, descriptor);
            }
        });
    }


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }
}
