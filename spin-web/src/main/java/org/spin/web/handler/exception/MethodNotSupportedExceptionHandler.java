package org.spin.web.handler.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.web.RestfulResponse;
import org.spin.web.handler.WebExceptionHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestMethodNotSupportedException;

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
public class MethodNotSupportedExceptionHandler implements WebExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(MethodNotSupportedExceptionHandler.class);

    @Override
    public RestfulResponse<?> handler(String appName, Throwable e, HttpServletRequest request) {
        String msg = String.format("不支持的请求方式: %s [%s]", ((HttpRequestMethodNotSupportedException) e).getMethod(), request.getRequestURI());
        logger.warn(msg);
        return RestfulResponse.<Void>error(ErrorCode.INTERNAL_ERROR, msg, e.getMessage())
            .withPath(appName + request.getRequestURI());
    }

    @Override
    public boolean support(Throwable e) {
        return e instanceof HttpRequestMethodNotSupportedException;
    }

    @Override
    public int order() {
        return 130;
    }
}
