package org.spin.web.handler.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.web.RestfulResponse;
import org.spin.web.handler.WebExceptionHalder;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import javax.servlet.http.HttpServletRequest;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/10/17</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class MediaTypeNotSupportedExceptionHandler implements WebExceptionHalder {
    private static final Logger logger = LoggerFactory.getLogger(MediaTypeNotSupportedExceptionHandler.class);

    @Override
    public RestfulResponse<Void> handler(Throwable e, HttpServletRequest request) {
        logger.warn("不支持的请求参数类型: {}", ((HttpMediaTypeNotSupportedException) e).getContentType());
        return RestfulResponse.error(ErrorCode.INVALID_PARAM, "不支持的请求参数类型", e.getMessage());
    }

    @Override
    public boolean support(Throwable e) {
        return e instanceof HttpMediaTypeNotSupportedException;
    }

    @Override
    public int order() {
        return Integer.MIN_VALUE + 8;
    }
}
