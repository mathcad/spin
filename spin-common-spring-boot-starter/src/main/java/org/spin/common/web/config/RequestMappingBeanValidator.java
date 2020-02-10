package org.spin.common.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.common.vo.RequestMappingInfoWrapper;
import org.spin.common.vo.ServiceRequestInfo;
import org.spin.common.web.annotation.Auth;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.lang.NonNull;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;
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

    @Value("${spring.application.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    @Value("${app.contextPath:${server.servlet.contextPath:}}")
    private String contextPath;

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
        List<RequestMappingInfoWrapper> requestMappingInfoWrappers = new LinkedList<>();

        Set<String> authNames = new HashSet<>();
        for (HandlerMapping handlerMapping : allRequestMappings.values()) {
            if (handlerMapping instanceof RequestMappingHandlerMapping) {
                RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping) handlerMapping;

                Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();

                for (Map.Entry<RequestMappingInfo, HandlerMethod> requestMappingInfoHandlerMethodEntry : handlerMethods.entrySet()) {
                    RequestMappingInfo requestMappingInfo = requestMappingInfoHandlerMethodEntry.getKey();
                    HandlerMethod handlerMethod = requestMappingInfoHandlerMethodEntry.getValue();

                    RequestMappingInfoWrapper mappingInfoWrapper = new RequestMappingInfoWrapper(requestMappingInfo, handlerMethod);
                    requestMappingInfoWrappers.add(mappingInfoWrapper);

                    if (needValidate(packages, handlerMethod.getMethod().getDeclaringClass())) {
                        Auth authAnnotation = AnnotatedElementUtils.getMergedAnnotation(handlerMethod.getMethod(), Auth.class);
                        if (null == authAnnotation) {
                            errMsg.add("Web接口" + mappingInfoWrapper.getBeanType() + "." + mappingInfoWrapper.getMethodName() + "[" + StringUtils.join(mappingInfoWrapper.getUrlPatterns(), ",") + "]未控制权限，存在安全隐患");
                        } else if (mappingInfoWrapper.getAuth() != AuthLevel.NONE) {
                            if (authNames.contains(mappingInfoWrapper.getAuthName())) {
                                errMsg.add("Web接口" + mappingInfoWrapper.getBeanType() + "[" + mappingInfoWrapper.getMethodName() + "]在当前Controller中使用了重复的权限名称，无法唯一标识");
                            }
                            authNames.add(mappingInfoWrapper.getAuthName());
                        }
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
        RequestMappingInfoHolder.setRequestInfo(new ServiceRequestInfo(appName.toUpperCase(), appVersion, contextPath, requestMappingInfoWrappers));

        try {
            Class.forName("org.springframework.kafka.core.KafkaTemplate");
            @SuppressWarnings("unchecked")
            KafkaTemplate<String, String> kafkaTemplate = applicationContext.getBean(KafkaTemplate.class);
            ListenableFuture<SendResult<String, String>> result = kafkaTemplate.send("gateway.mappingInfos", appName.toUpperCase(), JsonUtils.toJson(RequestMappingInfoHolder.getRequestInfo()));
            result.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                @Override
                public void onFailure(@NonNull Throwable ex) {
                    logger.warn("网关通知消息发送失败", ex);
                }

                @Override
                public void onSuccess(SendResult<String, String> result) {
                    logger.info("网关通知消息发送成功");
                }
            });
        } catch (NoSuchBeanDefinitionException | ClassNotFoundException ignore) {
            logger.warn("系统未启用KAFKA, 无法发起网关通知");
        } catch (BeansException e) {
            logger.warn("KAFKA初始化异常, 无法发起网关通知:", e);
        } catch (Exception e) {
            logger.warn("网关消息通知异常:", e);
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
