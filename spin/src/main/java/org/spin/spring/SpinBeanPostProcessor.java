package org.spin.spring;

import org.spin.core.SpinContext;
import org.spin.core.inspection.MethodDescriptor;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.MethodUtils;
import org.spin.core.util.ReflectionUtils;
import org.spin.core.util.StringUtils;
import org.spin.web.annotation.RestfulInterface;
import org.spin.web.annotation.RestfulMethod;
import org.spin.web.annotation.RestfulService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Bean后处理器
 * <p>Created by xuweinan on 2017/1/23.</p>
 */
@Component
public class SpinBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> cls = bean.getClass();
        Arrays.stream(cls.getInterfaces()).filter(i -> Objects.nonNull(i.getAnnotation(RestfulInterface.class))).findFirst().ifPresent(i -> {
            RestfulInterface anno = i.getAnnotation(RestfulInterface.class);
            final String module = StringUtils.isEmpty(anno.value()) ? beanName : anno.value();
            ReflectionUtils.doWithMethods(i.getClass(), method -> processRestMethod(bean, module, method));
        });
        Optional.ofNullable(cls.getAnnotation(RestfulService.class)).ifPresent(anno -> {
            final String module = StringUtils.isEmpty(anno.value()) ? beanName : anno.value();
            ReflectionUtils.doWithMethods(bean.getClass(), method -> processRestMethod(bean, module, method));
        });
        return bean;
    }

    private void processRestMethod(final Object bean, final String module, final Method method) {
        Optional.ofNullable(method.getAnnotation(RestfulMethod.class)).ifPresent((RestfulMethod anno) -> {
            final String service = StringUtils.isEmpty(anno.value()) ? method.getName() : anno.value();
            if (MethodUtils.containsGenericArg(method)) {
                throw new SimplifiedException("RestfulMethod注解的方法不能有泛型参数: " + method.getName() + "@" + bean.getClass());
            }
            List<MethodDescriptor> restMethod = SpinContext.getRestMethod(module, service);
            if (Objects.isNull(restMethod) || restMethod.stream().anyMatch(d -> method.equals(d.getMethod()))) {
                MethodDescriptor descriptor = new MethodDescriptor(method);
                descriptor.setTarget(null);
                SpinContext.addRestMethod(module, service, descriptor);
            }
        });
    }


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
