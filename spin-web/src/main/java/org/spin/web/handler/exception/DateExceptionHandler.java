package org.spin.web.handler.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.web.RestfulResponse;
import org.spin.web.handler.WebExceptionHalder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.format.DateTimeParseException;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/10/17</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Component
public class DateExceptionHandler implements WebExceptionHalder {
    private static final Logger logger = LoggerFactory.getLogger(DateExceptionHandler.class);

    @Override
    public RestfulResponse<Void> handler(String appName, Throwable e, HttpServletRequest request) {
        logger.warn("日期格式不正确: {}", e.getMessage());
        return RestfulResponse.<Void>error(ErrorCode.INVALID_PARAM, "日期格式不正确", e.getMessage())
            .withPath(appName + request.getRequestURI());
    }

    @Override
    public boolean support(Throwable e) {
        return e instanceof DateTimeParseException;
    }

    @Override
    public int order() {
        return 160;
    }
}
