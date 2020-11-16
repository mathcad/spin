package org.spin.web.handler.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.web.RestfulResponse;
import org.spin.web.handler.WebExceptionHandler;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import java.util.List;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/10/17</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Component
public class JavaxValidationExceptionHandler implements WebExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(JavaxValidationExceptionHandler.class);

    @Override
    public RestfulResponse<Void> handler(String appName, Throwable e, HttpServletRequest request) {
        logger.warn("请求[{}]中携带参数校验不通过: \n  {}", request.getRequestURI(), e.getMessage());
        List<ObjectError> errors = e instanceof BindException ? ((BindException) e).getAllErrors()
            : ((MethodArgumentNotValidException) e).getBindingResult().getAllErrors();


        return RestfulResponse.<Void>error(ErrorCode.INVALID_PARAM, e.getMessage())
            .withPath(appName + request.getRequestURI());
    }

    @Override
    public boolean support(Throwable e) {
        return e instanceof ValidationException;
    }

    @Override
    public int order() {
        return 155;
    }
}
