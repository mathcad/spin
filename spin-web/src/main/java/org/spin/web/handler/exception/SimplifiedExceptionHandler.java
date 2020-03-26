package org.spin.web.handler.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.throwable.SimplifiedException;
import org.spin.web.RestfulResponse;
import org.spin.web.handler.WebExceptionHalder;
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
public class SimplifiedExceptionHandler implements WebExceptionHalder {
    private static final Logger logger = LoggerFactory.getLogger(SimplifiedExceptionHandler.class);

    @Override
    public RestfulResponse<Void> handler(String appName, Throwable e, HttpServletRequest request) {
        logger.info(((SimplifiedException) e).getSimpleMessage(), e.getStackTrace()[0]);
        return RestfulResponse.<Void>error((SimplifiedException) e).withPath(appName + request.getRequestURI());
    }

    @Override
    public boolean support(Throwable e) {
        return e instanceof SimplifiedException;
    }

    @Override
    public int order() {
        return 100;
    }
}
