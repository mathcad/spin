package org.spin.web.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.BeanUtils;
import org.spin.core.util.MethodUtils;
import org.spin.core.util.StringUtils;
import org.spin.web.annotation.EncryptParameter;
import org.spin.web.converter.EncryptParamDecoder;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/6/3</p>
 * TODO: 未完成
 *
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
        String[] methodParamNames = MethodUtils.getMethodParamNames(method);
        for (int i = 0, parametersLength = parameters.length; i < parametersLength; i++) {
            Parameter parameter = parameters[i];
            if (null != parameter.getAnnotation(EncryptParameter.class) && !parameter.isAnnotationPresent(RequestBody.class)) {
                if (null == decoder) {
                    logger.warn("未配置参数解密实现, 无法解密参数:{} --> {}", method, methodParamNames[i]);
                } else {
                    if (null != parameter.getAnnotation(PathVariable.class)) {
                        Object pathVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
                        String value = BeanUtils.getFieldValue(pathVariables, methodParamNames[i]);
                        if (StringUtils.isNotEmpty(value)) {
                            try {
                                BeanUtils.setFieldValue(pathVariables, methodParamNames[i], decoder.decrypt(value));
                            } catch (Exception e) {
                                logger.warn("路径参数[" + methodParamNames[i] + "]解密失败: {}", e.getMessage());
                                throw new SimplifiedException("路径参数[" + methodParamNames[i] + "]解密失败");
                            }
                            request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, pathVariables);
                        }
                    }
                    String parameterStr = request.getParameter(methodParamNames[i]);
                    if (StringUtils.isNotEmpty(parameterStr)) {
                        try {
                            request.setAttribute(methodParamNames[i], decoder.decrypt(parameterStr));
                        } catch (Exception e) {
                            logger.warn("参数[" + methodParamNames[i] + "]解密失败: {}", e.getMessage());
                            throw new SimplifiedException("参数[" + methodParamNames[i] + "]解密失败");
                        }
                    }
                }
            }
        }
        return true;
    }
}
