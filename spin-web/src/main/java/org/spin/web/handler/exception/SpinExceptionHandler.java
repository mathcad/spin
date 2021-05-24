package org.spin.web.handler.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.throwable.SpinException;
import org.spin.web.RestfulResponse;
import org.spin.web.handler.WebExceptionHandler;
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
public class SpinExceptionHandler implements WebExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(SpinExceptionHandler.class);

    @Override
    public RestfulResponse<?> handler(String appName, Throwable e, HttpServletRequest request) {
        logger.info("系统异常", e);
        RestfulResponse<Object> response = RestfulResponse.error(((SpinException) e).getExceptionType(),
            ((SpinException) e).getSimpleMessage(), e.getMessage())
            .withPath(appName + request.getRequestURI());
        Object payload = ((SimplifiedException) e).getPayload();
        response.setData(payload);
        return response;
    }

    @Override
    public boolean support(Throwable e) {
        return e instanceof SpinException;
    }

    @Override
    public int order() {
        return 120;
    }
}
