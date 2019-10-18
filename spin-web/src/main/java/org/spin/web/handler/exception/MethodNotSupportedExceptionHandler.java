package org.spin.web.handler.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.web.RestfulResponse;
import org.spin.web.handler.WebExceptionHalder;
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
public class MethodNotSupportedExceptionHandler implements WebExceptionHalder {
    private static final Logger logger = LoggerFactory.getLogger(MethodNotSupportedExceptionHandler.class);

    @Override
    public RestfulResponse<Void> handler(Throwable e, HttpServletRequest request) {
        String msg = String.format("不支持的请求方式: %s [%s]", ((HttpRequestMethodNotSupportedException) e).getMethod(), request.getRequestURI());
        logger.warn(msg);
        return RestfulResponse.error(ErrorCode.INTERNAL_ERROR, msg, e.getMessage());
    }

    @Override
    public boolean support(Throwable e) {
        return e instanceof HttpRequestMethodNotSupportedException;
    }

    @Override
    public int order() {
        return Integer.MIN_VALUE + 2;
    }
}
