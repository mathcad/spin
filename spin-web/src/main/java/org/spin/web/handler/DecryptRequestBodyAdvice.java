package org.spin.web.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.web.annotation.EncryptParameter;
import org.spin.web.converter.EncryptParamDecoder;
import org.spin.web.http.DecryptHttpInputMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.lang.reflect.Type;

/**
 * 请求数据接收处理类<br>
 * <p>
 * 对加了@Decrypt的方法的数据进行解密操作<br>
 * <p>
 * 只对 @RequestBody 参数有效
 * todo: 未完成
 *
 * @author xiongshiyan
 */
//@ControllerAdvice
public class DecryptRequestBodyAdvice implements RequestBodyAdvice {
    private static final Logger logger = LoggerFactory.getLogger(DecryptRequestBodyAdvice.class);

    @Autowired(required = false)
    private EncryptParamDecoder decoder;

    @Override
    public boolean supports(@NonNull MethodParameter methodParameter, @NonNull Type targetType,
                            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return methodParameter.hasParameterAnnotation(EncryptParameter.class);
    }

    @Override
    public Object handleEmptyBody(Object body, @NonNull HttpInputMessage inputMessage, @NonNull MethodParameter parameter,
                                  @NonNull Type targetType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    @Override
    public @NonNull
    HttpInputMessage beforeBodyRead(@NonNull HttpInputMessage inputMessage, @NonNull MethodParameter parameter, @NonNull Type targetType,
                                    @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        if (null == decoder) {
            logger.warn("未配置参数解密实现, 无法解密参数:{} --> {}", parameter.getMethod(), parameter.getParameterName());
            return inputMessage;
        }
        return new DecryptHttpInputMessage(inputMessage, decoder);
    }

    @Override
    public @NonNull
    Object afterBodyRead(@NonNull Object body, @NonNull HttpInputMessage inputMessage, @NonNull MethodParameter parameter, @NonNull Type targetType,
                         @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }
}
