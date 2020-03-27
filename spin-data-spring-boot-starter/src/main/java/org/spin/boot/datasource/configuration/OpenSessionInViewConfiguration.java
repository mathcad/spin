package org.spin.boot.datasource.configuration;

import org.spin.boot.datasource.interceptor.OpenSessionInViewInterceptor;
import org.spin.boot.datasource.provider.DefaultSessionFactoryProvider;
import org.spin.boot.datasource.provider.SessionFactoryProvider;
import org.spin.boot.datasource.provider.WebSessionFactoryProvider;
import org.spin.core.util.StringUtils;
import org.spin.data.extend.MultiDataSourceConfig;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/3/26</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@AutoConfigureAfter(name = {"org.spin.boot.datasource.configuration.DataSourceAutoConfiguration"})
@ConditionalOnClass(name = "org.springframework.web.servlet.config.annotation.WebMvcConfigurer")
public class OpenSessionInViewConfiguration implements WebMvcConfigurer {

    private final MultiDataSourceConfig<?> config;

    public OpenSessionInViewConfiguration(MultiDataSourceConfig<?> config) {
        this.config = config;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (config.isOpenSessionInView()) {
            SessionFactoryProvider sessionFactoryProvider;
            try {
                Class.forName("org.spin.web.annotation.RequestDs");
                sessionFactoryProvider = new WebSessionFactoryProvider();
            } catch (ClassNotFoundException e) {
                sessionFactoryProvider = new DefaultSessionFactoryProvider();
            }

            InterceptorRegistration interceptorRegistration = registry.addInterceptor(new OpenSessionInViewInterceptor(sessionFactoryProvider))
                .addPathPatterns("/**");
            if (StringUtils.isNotBlank(config.getExcluePathPattern())) {
                interceptorRegistration.excludePathPatterns(StringUtils.split(config.getExcluePathPattern(), ","));
            }
            interceptorRegistration.order(Ordered.LOWEST_PRECEDENCE);
        }
    }
}
