package org.spin.spring.condition;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;

import java.util.Map;

/**
 * Created by xuweinan on 2017/2/7.
 * @author xuweinan
 */
public class OnBeanDefinationCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        if (metadata.isAnnotated(ConditionalOnBeanDefination.class.getName())) {
            Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnBeanDefination.class.getName());
            if (attributes.get("value") != null) {
                Class<?>[] classes = (Class<?>[]) attributes.get("value");
                if (classes.length > 0) {
                    String[] beanDefinations = context.getBeanFactory().getBeanDefinitionNames();
                    for (String beanName : beanDefinations) {
                        BeanDefinition definition = context.getRegistry().getBeanDefinition(beanName);
                        try {
                            if ((classes[0].isAssignableFrom(ClassUtils.forName(definition.getBeanClassName(), context.getClassLoader()))))
                                return true;
                        } catch (ClassNotFoundException e) {
                            return false;
                        }
                    }
                }
            }
            return false;
        } else
            return false;
    }
}
