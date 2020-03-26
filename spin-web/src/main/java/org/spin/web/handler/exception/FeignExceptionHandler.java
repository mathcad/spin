package org.spin.web.handler.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.web.RestfulResponse;
import org.spin.web.handler.WebExceptionHalder;
import org.spin.web.throwable.FeignHttpException;
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
public class FeignExceptionHandler implements WebExceptionHalder {
    private static final Logger logger = LoggerFactory.getLogger(FeignExceptionHandler.class);

    @Override
    public RestfulResponse<Void> handler(String appName, Throwable e, HttpServletRequest request) {
        logger.warn("远程调用失败: [{}]", e.getCause().getMessage());
        RestfulResponse<Void> res = ((FeignHttpException) e).toResponse();
        res.withPath(appName + request.getRequestURI() + " -> " + res.getPath());
        return res;
    }

    @Override
    public boolean support(Throwable e) {
        return e instanceof FeignHttpException;
    }

    @Override
    public int order() {
        return 90;
    }
}
