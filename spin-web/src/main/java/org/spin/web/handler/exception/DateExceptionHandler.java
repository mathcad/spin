package org.spin.web.handler.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.web.RestfulResponse;
import org.spin.web.handler.WebExceptionHalder;

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
public class DateExceptionHandler implements WebExceptionHalder {
    private static final Logger logger = LoggerFactory.getLogger(DateExceptionHandler.class);

    @Override
    public RestfulResponse<Void> handler(Throwable e, HttpServletRequest request) {
        logger.warn("日期格式不正确: {}", e.getMessage());
        return RestfulResponse.error(ErrorCode.INVALID_PARAM, "日期格式不正确", e.getMessage());
    }

    @Override
    public boolean support(Throwable e) {
        return e instanceof DateTimeParseException;
    }

    @Override
    public int order() {
        return Integer.MIN_VALUE + 5;
    }
}
