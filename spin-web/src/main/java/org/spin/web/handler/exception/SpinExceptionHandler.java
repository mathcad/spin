package org.spin.web.handler.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.throwable.SpinException;
import org.spin.web.RestfulResponse;
import org.spin.web.handler.WebExceptionHalder;

import javax.servlet.http.HttpServletRequest;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/10/17</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class SpinExceptionHandler implements WebExceptionHalder {
    private static final Logger logger = LoggerFactory.getLogger(SpinExceptionHandler.class);

    @Override
    public RestfulResponse<Void> handler(Throwable e, HttpServletRequest request) {
        logger.info("系统异常", e);
        return RestfulResponse.error(((SpinException) e).getExceptionType(), ((SpinException) e).getSimpleMessage(), e.getMessage());
    }

    @Override
    public boolean support(Throwable e) {
        return e instanceof SpinException;
    }

    @Override
    public int order() {
        return Integer.MIN_VALUE + 1;
    }
}
