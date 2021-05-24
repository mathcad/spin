package org.spin.web.handler.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.web.RestfulResponse;
import org.spin.web.handler.WebExceptionHandler;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/10/17</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Component
public class HttpMessageConversionExceptionHandler implements WebExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(HttpMessageConversionExceptionHandler.class);

    @Override
    public RestfulResponse<?> handler(String appName, Throwable e, HttpServletRequest request) {
        logger.warn("请求参数转换失败: {}", e.getMessage());
        return RestfulResponse.<Void>error(ErrorCode.INVALID_PARAM, "请求参数转换失败", e.getMessage())
            .withPath(appName + request.getRequestURI());
    }

    @Override
    public boolean support(Throwable e) {
        return e instanceof HttpMessageConversionException;
    }

    @Override
    public int order() {
        return 180;
    }
}
