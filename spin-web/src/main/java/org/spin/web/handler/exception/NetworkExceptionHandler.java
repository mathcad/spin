package org.spin.web.handler.exception;

import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.web.RestfulResponse;
import org.spin.web.handler.WebExceptionHalder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.net.ConnectException;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/10/17</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Component
public class NetworkExceptionHandler implements WebExceptionHalder {
    private static final Logger logger = LoggerFactory.getLogger(NetworkExceptionHandler.class);

    @Override
    public RestfulResponse<Void> handler(String appName, Throwable e, HttpServletRequest request) {
        logger.warn("网络连接错误: [{}]", e.getMessage());
        String tmp = e.getCause().getMessage().toLowerCase();
        StringBuilder msg = new StringBuilder();
        if (tmp.contains("refused")) {
            msg.append("网络错误, 连接被拒绝");
        } else if (tmp.contains("timed out") || tmp.contains("timeout")) {
            msg.append("网络错误, 连接超时");
        } else {
            msg.append("网络错误");
        }
        if (e instanceof HttpHostConnectException) {
            msg.append(": [").append(((HttpHostConnectException) e).getHost().toString()).append("]");
        }
        return RestfulResponse.<Void>error(ErrorCode.NETWORK_EXCEPTION, msg.toString(), e.getMessage())
            .withPath(appName + request.getRequestURI());
    }

    @Override
    public boolean support(Throwable e) {
        return e instanceof ConnectException;
    }

    @Override
    public int order() {
        return 200;
    }
}
