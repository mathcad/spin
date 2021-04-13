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
import org.springframework.web.bind.MissingPathVariableException;

import javax.servlet.http.HttpServletRequest;

/**
 * 路径参数类型不匹配异常处理器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/12/31</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Component
public class MismatchPathVariableExceptionHandler implements WebExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(MismatchPathVariableExceptionHandler.class);

    @Override
    public RestfulResponse<Void> handler(String appName, Throwable e, HttpServletRequest request) {
        logger.info("参数类型转换失败: {}", e.getMessage());
        MissingPathVariableException cause = (MissingPathVariableException) e;
        String name = cause.getVariableName();
        String requireType = cause.getParameter().getParameterType().getSimpleName();
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
                    argName = ArrayUtils.detect(apiImplicitParams.value(), it -> name.equals(it.name()))
                        .map(ApiImplicitParam::value).orElse(null);
                }
            } else {
                argName = apiImplicitParam.value();
            }
        } else {
            argName = apiParam.value();
        }
        argName = StringUtils.isEmpty(argName) ? name : argName;
        return RestfulResponse.<Void>error(ErrorCode.INVALID_PARAM, String.format("路径参数[%s]解析失败, 无法获取%s类型的参数", argName, requireType))
            .withPath(appName + request.getRequestURI());
    }

    @Override
    public boolean support(Throwable e) {
        return e instanceof MissingPathVariableException;
    }

    @Override
    public int order() {
        return 220;
    }
}
