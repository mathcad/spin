package org.spin.web.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.util.StringUtils;
import org.spin.web.annotation.EncryptParameter;
import org.spin.web.converter.EncryptParamDecoder;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/6/3</p>
 * TODO: 未完成
 * @author xuweinan
 * @version 1.0
 */
public class EncryptParameterInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(EncryptParameterInterceptor.class);
    private final EncryptParamDecoder decoder;

    public EncryptParameterInterceptor(EncryptParamDecoder decoder) {
        this.decoder = decoder;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // 是否调用方法
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        Method method = ((HandlerMethod) handler).getMethod();
        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            if (null != parameter.getAnnotation(EncryptParameter.class)) {
                if (null == decoder) {
                    logger.warn("未配置参数解密实现, 无法解密参数:{} --> {}", method, parameter.getName());
                } else {
                    if (null != parameter.getAnnotation(PathVariable.class)) {
                        Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
                        String value = (String) pathVariables.get("code");
                    }
                    String parameterStr = request.getParameter(parameter.getName());
                    if (StringUtils.isNotEmpty(parameterStr)) {
                        decoder.decrypt(parameterStr);
                    }
                }
            }
        }

        request.getRequestDispatcher(request.getRequestURI()).forward(request, response);
        return true;
    }
}
