package org.spin.cloud.web.handler.exception;

import feign.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.web.RestfulResponse;
import org.spin.web.handler.WebExceptionHandler;
import org.spin.web.handler.exception.AssertExceptionHandler;
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
public class RetryableExceptionHandler implements WebExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(AssertExceptionHandler.class);

    @Override
    public RestfulResponse<Void> handler(String appName, Throwable e, HttpServletRequest request) {
        logger.warn("Feign客户端调用错误: [{}]", e.getMessage());
        String tmp = e.getCause().getMessage().toLowerCase();
        StringBuilder msg = new StringBuilder();
        if (tmp.contains("refused")) {
            msg.append("远程服务调用错误, 连接被拒绝");
        } else if (tmp.contains("timed out") || tmp.contains("timeout")) {
            msg.append("远程服务调用错误, 连接超时");
        } else {
            msg.append("远程服务调用错误");
        }
        return RestfulResponse.<Void>error(ErrorCode.NETWORK_EXCEPTION, msg.toString(), e.getMessage())
            .withPath(appName + request.getRequestURI());
    }

    @Override
    public boolean support(Throwable e) {
        return e instanceof RetryableException;
    }

    @Override
    public int order() {
        return 200;
    }
}
