package org.spin.common.web.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.common.web.RestfulResponse;
import org.spin.common.web.annotation.Auth;
import org.spin.common.web.annotation.Plain;
import org.spin.core.util.BeanUtils;
import org.spin.core.util.ReflectionUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodProcessor;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * MVC返回结果处理器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/20</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class WrappedRequestResponseBodyProcessor implements HandlerMethodReturnValueHandler {

    private static final Logger logger = LoggerFactory.getLogger(WrappedRequestResponseBodyProcessor.class);
    private static Method writeWithMessageConverters;

    private RequestResponseBodyMethodProcessor target;
    private List<HttpMessageConverter<?>> messageConverters;
    private final List<MediaType> allSupportedMediaTypes;

    static {
        Method[] declaredMethods = AbstractMessageConverterMethodProcessor.class.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            if (declaredMethod.getName().equals("writeWithMessageConverters") && declaredMethod.getParameterCount() == 4) {
                writeWithMessageConverters = declaredMethod;
                break;
            }
        }
        Assert.notNull(writeWithMessageConverters, "未找到合适的Response写出方法");
        ReflectionUtils.makeAccessible(writeWithMessageConverters);
    }

    public WrappedRequestResponseBodyProcessor(RequestResponseBodyMethodProcessor target) {
        this.target = target;
        this.messageConverters = BeanUtils.getFieldValue(target, "messageConverters");
        this.allSupportedMediaTypes = getAllSupportedMediaTypes(this.messageConverters);

    }


    @Override
    public boolean supportsReturnType(@NonNull MethodParameter returnType) {
        return (AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), ResponseBody.class) || returnType.hasMethodAnnotation(ResponseBody.class))
            && (AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), Auth.class) || returnType.hasMethodAnnotation(Auth.class))
            && !AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), Plain.class)
            && !returnType.hasMethodAnnotation(Plain.class);
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws IOException, HttpMediaTypeNotAcceptableException {
        mavContainer.setRequestHandled(true);
        ServletServerHttpRequest inputMessage = createInputMessage(webRequest);
        ServletServerHttpResponse outputMessage = createOutputMessage(webRequest);

        outputMessage.getHeaders().add("Encoded", "1");
        // Try even with null return value. ResponseBodyAdvice could get involved.
        if (returnValue instanceof RestfulResponse) {
            try {
                writeWithMessageConverters.invoke(target, returnValue, returnType, inputMessage, outputMessage);
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error("Resopnse响应写出异常:", e);
            }
        } else {
            writeWithMessageConverters(RestfulResponse.ok(returnValue), outputMessage);
        }
    }

    /**
     * Create a new {@link HttpInputMessage} from the given {@link NativeWebRequest}.
     *
     * @param webRequest the web request to create an input message from
     * @return the input message
     */
    private ServletServerHttpRequest createInputMessage(NativeWebRequest webRequest) {
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        Assert.state(servletRequest != null, "No HttpServletRequest");
        return new ServletServerHttpRequest(servletRequest);
    }

    /**
     * Creates a new {@link HttpOutputMessage} from the given {@link NativeWebRequest}.
     *
     * @param webRequest the web request to create an output message from
     * @return the output message
     */
    private ServletServerHttpResponse createOutputMessage(NativeWebRequest webRequest) {
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        Assert.state(response != null, "No HttpServletResponse");
        return new ServletServerHttpResponse(response);
    }

    @SuppressWarnings("unchecked")
    private void writeWithMessageConverters(RestfulResponse<?> value, ServletServerHttpResponse outputMessage)
        throws IOException, HttpMediaTypeNotAcceptableException {

        MediaType selectedMediaType = MediaType.APPLICATION_JSON_UTF8.removeQualityValue();

        for (HttpMessageConverter<?> converter : this.messageConverters) {
            GenericHttpMessageConverter<RestfulResponse<?>> genericConverter = (converter instanceof GenericHttpMessageConverter ? (GenericHttpMessageConverter<RestfulResponse<?>>) converter : null);
            if (genericConverter != null ? genericConverter.canWrite(RestfulResponse.class, RestfulResponse.class, selectedMediaType) : converter.canWrite(RestfulResponse.class, selectedMediaType)) {
                if (value != null) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Writing [{}]", value);
                    }
                    if (genericConverter != null) {
                        genericConverter.write(value, RestfulResponse.class, selectedMediaType, outputMessage);
                    } else {
                        ((HttpMessageConverter<RestfulResponse<?>>) converter).write(value, selectedMediaType, outputMessage);
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Nothing to write: null body");
                    }
                }
                return;
            }
        }

        if (value != null) {
            throw new HttpMediaTypeNotAcceptableException(this.allSupportedMediaTypes);
        }
    }

    private List<MediaType> getAllSupportedMediaTypes(List<HttpMessageConverter<?>> messageConverters) {
        Set<MediaType> mediaTypes = new LinkedHashSet<>();
        for (HttpMessageConverter<?> messageConverter : messageConverters) {
            mediaTypes.addAll(messageConverter.getSupportedMediaTypes());
        }
        List<MediaType> result = new ArrayList<>(mediaTypes);
        MediaType.sortBySpecificity(result);
        return Collections.unmodifiableList(result);
    }
}
