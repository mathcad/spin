package org.spin.web.handler.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.web.RestfulResponse;
import org.spin.web.handler.WebExceptionHalder;
import org.springframework.http.converter.HttpMessageNotReadableException;

import javax.servlet.http.HttpServletRequest;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/10/17</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class HttpParamExceptionHandler implements WebExceptionHalder {
    private static final Logger logger = LoggerFactory.getLogger(HttpParamExceptionHandler.class);

    @Override
    public RestfulResponse<Void> handler(Throwable e, HttpServletRequest request) {
        if (e.getMessage().startsWith("Required request body")) {
            logger.warn("请求体中缺失参数: {}", e.getMessage());
            return RestfulResponse.error(ErrorCode.INVALID_PARAM, "请求体中缺失Request Body参数", e.getMessage());
        } else {
            logger.warn("请求体中参数不合法: {}", e.getMessage());
            return RestfulResponse.error(ErrorCode.INVALID_PARAM, "请求体中参数不合法", e.getMessage());
        }
    }

    @Override
    public boolean support(Throwable e) {
        return e instanceof HttpMessageNotReadableException;
    }

    @Override
    public int order() {
        return Integer.MIN_VALUE + 6;
    }
}
