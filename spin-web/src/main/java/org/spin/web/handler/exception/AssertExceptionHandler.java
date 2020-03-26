package org.spin.web.handler.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.core.throwable.AssertFailException;
import org.spin.web.RestfulResponse;
import org.spin.web.handler.WebExceptionHalder;
import org.spin.web.throwable.FeignHttpException;
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
public class AssertExceptionHandler implements WebExceptionHalder {
    private static final Logger logger = LoggerFactory.getLogger(AssertExceptionHandler.class);

    @Override
    public RestfulResponse<Void> handler(String appName, Throwable e, HttpServletRequest request) {
        logger.info(e.getMessage(), e.getStackTrace()[0]);
        return RestfulResponse.<Void>error(ErrorCode.ASSERT_FAIL, ((AssertFailException) e).getSimpleMessage())
            .withPath(appName + request.getRequestURI());
    }

    @Override
    public boolean support(Throwable e) {
        return e instanceof AssertFailException;
    }

    @Override
    public int order() {
        return 110;
    }
}
