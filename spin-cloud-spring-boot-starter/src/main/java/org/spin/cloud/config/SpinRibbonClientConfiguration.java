package org.spin.cloud.config;

import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.GrayRuleBuilder;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.ZoneAvoidanceRule;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.ReflectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.cloud.netflix.ribbon.RibbonClientConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonClientSpecification;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import java.lang.reflect.Field;

/**
 * Ribbon客户端自动配置
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/9/17</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Configuration
@AutoConfigureAfter(RibbonClientConfiguration.class)
public class SpinRibbonClientConfiguration {

    @Bean
    public RibbonClientSpecification spinSpecification() {
        return new RibbonClientSpecification("default.org.spin.cloud.config.SpinRibbonClientConfiguration", CollectionUtils.ofArray(SpinRibbonClientConfiguration.class));
    }

    @Bean
    public BeanPostProcessor loadBalancerModifier() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(@NonNull Object bean, String beanName) throws BeansException {

                if (bean instanceof BaseLoadBalancer) {
                    IRule iRule = ((BaseLoadBalancer) bean).getRule();
                    if (iRule instanceof ZoneAvoidanceRule) {
                        Field compositePredicate = ReflectionUtils.findField(ZoneAvoidanceRule.class, "compositePredicate");
                        ReflectionUtils.makeAccessible(compositePredicate);
                        ReflectionUtils.setField(compositePredicate, iRule, GrayRuleBuilder.createCompositePredicate(iRule));
                    }
                }
                return bean;
            }
        };
    }
}
