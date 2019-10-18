package org.spin.web.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.web.RestfulResponse;
import org.spin.web.handler.exception.DateExceptionHandler;
import org.spin.web.handler.exception.HttpMessageConversionExceptionHandler;
import org.spin.web.handler.exception.HttpParamExceptionHandler;
import org.spin.web.handler.exception.MediaTypeNotSupportedExceptionHandler;
import org.spin.web.handler.exception.MethodNotSupportedExceptionHandler;
import org.spin.web.handler.exception.SQLIntegrityConstraintViolationExceptionHandler;
import org.spin.web.handler.exception.SimplifiedExceptionHandler;
import org.spin.web.handler.exception.SpinExceptionHandler;
import org.spin.web.handler.exception.ValidationExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Web层全局异常处理
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/10/30</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@ControllerAdvice
public class GlobalExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionAdvice.class);

    @Value("${spring.profiles.active:dev}")
    private String env;

    private final List<WebExceptionHalder> handlers = new LinkedList<>();

    private boolean isPro;

    public GlobalExceptionAdvice(List<WebExceptionHalder> handlers) {
        this.handlers.add(new SimplifiedExceptionHandler());
        this.handlers.add(new SpinExceptionHandler());
        this.handlers.add(new MethodNotSupportedExceptionHandler());
        this.handlers.add(new SQLIntegrityConstraintViolationExceptionHandler());
        this.handlers.add(new ValidationExceptionHandler());
        this.handlers.add(new DateExceptionHandler());
        this.handlers.add(new HttpParamExceptionHandler());
        this.handlers.add(new HttpMessageConversionExceptionHandler());
        this.handlers.add(new MediaTypeNotSupportedExceptionHandler());
        handlers.sort(Comparator.comparing(WebExceptionHalder::order));
        this.handlers.addAll(handlers);
    }

    @PostConstruct
    public void init() {
        isPro = this.env.toLowerCase().contains("pro");
    }

    /**
     * 全局异常捕捉处理
     *
     * @param e        异常对象
     * @param request  请求
     * @param response 响应
     * @return 响应请求
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public RestfulResponse<Void> errorHandler(Exception e, HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Encoded", "1");

        Throwable cause = e;
        int depth = 0;
        RestfulResponse<Void> res;
        while (cause != null && depth < 30) {
            ++depth;
            for (WebExceptionHalder handler : handlers) {
                if (handler.support(cause)) {
                    res = handler.handler(cause, request);
                    res.setPath(request.getRequestURI());
                    return res;
                }
            }

            cause = cause.getCause();
        }

        logger.warn("Controller返回发生未处理异常", e);
        RestfulResponse<Void> error = RestfulResponse.error(ErrorCode.INTERNAL_ERROR);
        error.setPath(request.getRequestURI());
        if (!isPro) {
            error.setError(e.getMessage() + "\n" + e.getStackTrace()[0].toString());
        }
        return error;
    }
}
