package org.spin.common.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.common.web.annotation.Auth;
import org.spin.core.util.StringUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.NonNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 项目启动检测
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/15</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class RequestMappingBeanValidator implements ApplicationContextAware, ApplicationListener {
    private static final Logger logger = LoggerFactory.getLogger(RequestMappingBeanValidator.class);

    private ApplicationContext applicationContext;

    @Value("${spring.auth.strict:true}")
    private boolean authStrict = true;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationEvent event) {
        if (event instanceof ApplicationReadyEvent) {
            validateRequestMappingBeans();
        }
    }

    private void validateRequestMappingBeans() {

        Map<String, HandlerMapping> allRequestMappings = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, HandlerMapping.class, true, false);
        List<String> errMsg = new LinkedList<>();
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(SpringBootApplication.class);
        Set<String> packages = beansWithAnnotation.values().stream().map(Object::getClass).map(Class::getPackage).map(Package::getName).collect(Collectors.toSet());
        for (HandlerMapping handlerMapping : allRequestMappings.values()) {
            if (handlerMapping instanceof RequestMappingHandlerMapping) {
                RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping) handlerMapping;

                Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();

                for (Map.Entry<RequestMappingInfo, HandlerMethod> requestMappingInfoHandlerMethodEntry : handlerMethods.entrySet()) {
                    RequestMappingInfo requestMappingInfo = requestMappingInfoHandlerMethodEntry.getKey();
                    HandlerMethod handlerMethod = requestMappingInfoHandlerMethodEntry.getValue();

                    if (null == AnnotatedElementUtils.getMergedAnnotation(handlerMethod.getMethod(), Auth.class) && needValidate(packages, handlerMethod.getMethod().getDeclaringClass())) {
                        errMsg.add("Web接口" + handlerMethod.getMethod().getDeclaringClass().getSimpleName() + "." + handlerMethod.getMethod().getName() + "[" + StringUtils.join(requestMappingInfo.getPatternsCondition().getPatterns(), ",") + "]未控制权限，存在安全隐患");
                    }
                }
            }
        }

        if (!errMsg.isEmpty()) {
            for (String err : errMsg) {
                logger.error(err);
            }
            if (authStrict) {
                logger.error("系统启动过程中出现非法行为，即将关闭");
                System.exit(-1);
            }
        }
        logger.info("系统启动完成");
    }

    private boolean needValidate(Set<String> packages, Class cls) {
        for (String p : packages) {
            if (cls.getName().startsWith(p)) {
                return true;
            }
        }
        return false;
    }
}
