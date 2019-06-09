package org.spin.common.web.handler;

import feign.RetryableException;
import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.common.throwable.FeignHttpException;
import org.spin.common.web.RestfulResponse;
import org.spin.core.ErrorCode;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.BooleanExt;
import org.spin.core.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ValidationException;
import java.net.ConnectException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.format.DateTimeParseException;
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

    private boolean isPro;

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
        while (cause != null && depth < 30) {
            ++depth;
            if (cause instanceof SimplifiedException) {
                logger.info(((SimplifiedException) cause).getSimpleMessage(), cause.getStackTrace()[0]);
                return RestfulResponse.error((SimplifiedException) cause);
            } else if (cause instanceof HttpRequestMethodNotSupportedException) {
                String msg = String.format("不支持的请求类型: %s [%s]", ((HttpRequestMethodNotSupportedException) cause).getMethod(), request.getRequestURI());
                logger.warn(msg);
                return RestfulResponse.error(ErrorCode.INTERNAL_ERROR, msg, cause.getMessage());
            } else if (cause instanceof SQLIntegrityConstraintViolationException) {
                String msg = "数据重复";
                logger.warn(msg, cause);
                return RestfulResponse.error(ErrorCode.INTERNAL_ERROR, msg, cause.getMessage());
            } else if (cause instanceof BindException || cause instanceof MethodArgumentNotValidException) {
                return bindExceptionHandler((Exception) cause, request, response);
            } else if (cause instanceof DateTimeParseException) {
                logger.warn("日期格式不正确: {}", cause.getMessage());
                return RestfulResponse.error(ErrorCode.INVALID_PARAM, "日期格式不正确", cause.getMessage());
            } else if (cause instanceof HttpMessageNotReadableException) {
                if (cause.getMessage().startsWith("Required request body")) {
                    logger.warn("请求体中缺失参数: {}", cause.getMessage());
                    return RestfulResponse.error(ErrorCode.INVALID_PARAM, "请求体中缺失Request Body参数", cause.getMessage());
                } else {
                    logger.warn("请求体中参数不合法: {}", cause.getMessage());
                    return RestfulResponse.error(ErrorCode.INVALID_PARAM, "请求体中参数不合法", cause.getMessage());
                }
            } else if (cause instanceof HttpMessageConversionException) {
                logger.warn("请求参数转换失败: {}", cause.getMessage());
                return RestfulResponse.error(ErrorCode.INVALID_PARAM, "请求参数转换失败", cause.getMessage());
            } else if (cause instanceof HttpMediaTypeNotSupportedException) {
                logger.warn("不支持的请求参数类型: {}", ((HttpMediaTypeNotSupportedException) cause).getContentType());
                return RestfulResponse.error(ErrorCode.INVALID_PARAM, "不支持的请求参数类型", cause.getMessage());
            } else if (cause instanceof FeignHttpException) {
                logger.warn("远程调用失败: [{}]", cause.getCause().getMessage());
                return ((FeignHttpException) cause).toResponse();
            } else if (cause instanceof ConnectException) {
                logger.warn("网络连接错误: [{}]", cause.getMessage());
                String tmp = cause.getCause().getMessage().toLowerCase();
                StringBuilder msg = new StringBuilder();
                if (tmp.contains("refused")) {
                    msg.append("网络错误, 连接被拒绝");
                } else if (tmp.contains("timed out") || tmp.contains("timeout")) {
                    msg.append("网络错误, 连接超时");
                } else {
                    msg.append("网络错误");
                }
                if (cause instanceof HttpHostConnectException) {
                    msg.append(": [").append(((HttpHostConnectException) cause).getHost().toString()).append("]");
                }
                return RestfulResponse.error(ErrorCode.NETWORK_EXCEPTION, msg.toString(), cause.getMessage());
            } else if (cause instanceof RetryableException) {
                logger.warn("Feign客户端调用错误: [{}]", cause.getMessage());
                String tmp = cause.getCause().getMessage().toLowerCase();
                StringBuilder msg = new StringBuilder();
                if (tmp.contains("refused")) {
                    msg.append("远程服务调用错误, 连接被拒绝");
                } else if (tmp.contains("timed out") || tmp.contains("timeout")) {
                    msg.append("远程服务调用错误, 连接超时");
                } else {
                    msg.append("远程服务调用错误");
                }
                return RestfulResponse.error(ErrorCode.NETWORK_EXCEPTION, msg.toString(), cause.getMessage());
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

    /**
     * validation 异常处理
     * <p>处理请求参数格式错误 @RequestBody MethodArgumentNotValidException</p>
     *
     * @param exception validation BindException
     * @param request   请求
     * @param response  响应
     * @return 响应结果
     */
    @ResponseBody
    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class, ValidationException.class})
    public RestfulResponse<Void> bindExceptionHandler(Exception exception, HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Encoded", "1");

        logger.warn("请求[{}]中携带参数校验不通过: \n  {}", request.getRequestURI(), exception.getMessage());
        List<ObjectError> errors = exception instanceof BindException ? ((BindException) exception).getAllErrors()
            : ((MethodArgumentNotValidException) exception).getBindingResult().getAllErrors();

        String errorMessage = BooleanExt.ofAny(CollectionUtils.isEmpty(errors))
            .no(() -> errors.stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .reduce((a, b) -> a + "," + b).orElse(""))
            .get();
        RestfulResponse<Void> restfulResponse = RestfulResponse.error(ErrorCode.INVALID_PARAM, errorMessage);
        restfulResponse.setError("Bad Request");
        restfulResponse.setPath(request.getRequestURI());
        return restfulResponse;
    }
}
