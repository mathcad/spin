package org.spin.web.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.core.util.StringUtils;
import org.spin.web.RestfulResponse;
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

    @Value("${spring.application.name:}")
    private String appName;

    private final List<WebExceptionHandler> handlers = new LinkedList<>();

    private boolean isPro;

    public GlobalExceptionAdvice(List<WebExceptionHandler> handlers) {
        handlers.sort(Comparator.comparing(WebExceptionHandler::order));
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
        boolean isInternal = StringUtils.isNotEmpty(request.getHeader("X-App-Name"));
        while (cause != null && depth < 30) {
            ++depth;
            for (WebExceptionHandler handler : handlers) {
                if (handler.support(cause)) {
                    res = handler.handler(appName, cause, request);
                    if (null == res.getPath()) {
                        res.setPath(request.getRequestURI());
                    }
                    if (isInternal) {
                        response.setStatus(res.getStatus() > 0 ? res.getStatus() : ErrorCode.INTERNAL_ERROR.getCode());
                    }
                    return res;
                }
            }

            cause = cause.getCause();
        }

        logger.warn("Controller返回发生未处理异常", e);
        RestfulResponse<Void> error = RestfulResponse.<Void>error(ErrorCode.INTERNAL_ERROR)
            .withPath(appName + request.getRequestURI());
        if (isInternal) {
            response.setStatus(ErrorCode.INTERNAL_ERROR.getCode());
        }
        error.setPath(request.getRequestURI());
        if (!isPro) {
            error.setError(e.getMessage() + "\n" + e.getStackTrace()[0].toString());
        }
        return error;
    }
}
