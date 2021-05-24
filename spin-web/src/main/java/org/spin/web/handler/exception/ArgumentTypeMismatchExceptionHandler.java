package org.spin.web.handler.exception;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.core.util.ArrayUtils;
import org.spin.core.util.StringUtils;
import org.spin.web.RestfulResponse;
import org.spin.web.handler.WebExceptionHandler;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;

/**
 * 参数类型不匹配异常处理器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/12/31</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Component
public class ArgumentTypeMismatchExceptionHandler implements WebExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ArgumentTypeMismatchExceptionHandler.class);

    @Override
    public RestfulResponse<?> handler(String appName, Throwable e, HttpServletRequest request) {
        logger.info("参数类型转换失败: {}", e.getMessage());
        MethodArgumentTypeMismatchException cause = (MethodArgumentTypeMismatchException) e;
        String name = cause.getName();
        String requireType = cause.getRequiredType() == null ? cause.getParameter().getParameterType().getSimpleName()
            : cause.getRequiredType().getSimpleName();
        String actualValue = StringUtils.toStringEmpty(cause.getValue());
        MethodParameter parameter = cause.getParameter();

        String argName;
        ApiParam apiParam = parameter.getParameterAnnotation(ApiParam.class);
        if (null == apiParam) {
            ApiImplicitParam apiImplicitParam = parameter.getMethodAnnotation(ApiImplicitParam.class);
            if (null == apiImplicitParam) {
                ApiImplicitParams apiImplicitParams = parameter.getMethodAnnotation(ApiImplicitParams.class);
                if (null == apiImplicitParams || apiImplicitParams.value().length == 0) {
                    argName = null;
                } else {
                    argName = ArrayUtils.detect(apiImplicitParams.value(), it -> name.equals(it.name())).map(ApiImplicitParam::value).orElse(null);
                }
            } else {
                argName = apiImplicitParam.value();
            }
        } else {
            argName = apiParam.value();
        }
        argName = StringUtils.isEmpty(argName) ? name : argName;
        return RestfulResponse.<Void>error(ErrorCode.INVALID_PARAM, String.format("参数[%s]类型不匹配, 无法将\"%s\"转换为%s类型", argName, actualValue, requireType))
            .withPath(appName + request.getRequestURI());
    }

    @Override
    public boolean support(Throwable e) {
        return e instanceof MethodArgumentTypeMismatchException;
    }

    @Override
    public int order() {
        return 210;
    }
}
