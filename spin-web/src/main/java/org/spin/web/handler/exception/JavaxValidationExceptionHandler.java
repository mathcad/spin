package org.spin.web.handler.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.web.RestfulResponse;
import org.spin.web.handler.WebExceptionHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

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
    public RestfulResponse<?> handler(String appName, Throwable e, HttpServletRequest request) {
        logger.warn("请求[{}]中携带参数校验不通过: \n  {}", request.getRequestURI(), e.getMessage());

        StringBuilder msg = new StringBuilder();
        if (e instanceof ConstraintViolationException) {
            for (ConstraintViolation<?> constraintViolation : ((ConstraintViolationException) e).getConstraintViolations()) {
                msg.append(constraintViolation.getMessage()).append(", ");
            }
            if (msg.length() > 2) {
                msg.setLength(msg.length() - 2);
            } else {
                msg.append(e.getMessage());
            }
        } else {
            msg.append(e.getMessage());
        }
        return RestfulResponse.<Void>error(ErrorCode.INVALID_PARAM, msg.toString())
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
