package org.spin.web.handler.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.core.util.BooleanExt;
import org.spin.core.util.CollectionUtils;
import org.spin.web.RestfulResponse;
import org.spin.web.handler.WebExceptionHandler;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.servlet.http.HttpServletRequest;
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
public class ValidationExceptionHandler implements WebExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ValidationExceptionHandler.class);

    @Override
    public RestfulResponse<?> handler(String appName, Throwable e, HttpServletRequest request) {
        logger.warn("请求[{}]中携带参数校验不通过: \n  {}", request.getRequestURI(), e.getMessage());
        List<ObjectError> errors = e instanceof BindException ? ((BindException) e).getAllErrors()
            : ((MethodArgumentNotValidException) e).getBindingResult().getAllErrors();

        String errorMessage = BooleanExt.ofAny(CollectionUtils.isEmpty(errors))
            .no(() -> errors.stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .reduce((a, b) -> a + "," + b).orElse(""))
            .get();

        return RestfulResponse.<Void>error(ErrorCode.INVALID_PARAM, errorMessage)
            .withPath(appName + request.getRequestURI());
    }

    @Override
    public boolean support(Throwable e) {
        return e instanceof BindException || e instanceof MethodArgumentNotValidException;
    }

    @Override
    public int order() {
        return 150;
    }
}
