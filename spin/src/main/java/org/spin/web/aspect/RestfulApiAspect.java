package org.spin.web.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.core.session.SessionUser;
import org.spin.core.SpinContext;
import org.spin.core.auth.Authenticator;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.StringUtils;
import org.spin.core.session.SessionManager;
import org.spin.web.RestfulResponse;
import org.spin.web.annotation.Needed;
import org.spin.web.annotation.RestfulApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * Restful切面
 * <p>Created by xuweinan on 2016/10/2.</p>
 *
 * @author xuweinan
 */
@Aspect
@Component
public class RestfulApiAspect implements Ordered {
    private static final Logger logger = LoggerFactory.getLogger(RestfulApiAspect.class);

    @Autowired(required = false)
    private Authenticator authenticator;

    @Pointcut("execution(org.spin.web.RestfulResponse *.*(..)) && @annotation(org.spin.web.annotation.RestfulApi)")
    private void restfulMethod() {
    }

    @Around("restfulMethod()")
    public Object restfulAround(ProceedingJoinPoint joinPoint) {
        boolean isAllowed = false;
        Method apiMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RestfulApi anno = apiMethod.getAnnotation(RestfulApi.class);
        boolean needAuth = anno.auth();
        if (needAuth) {
            String authRouter = StringUtils.isEmpty(anno.authRouter()) ? anno.name() : anno.authRouter();
            SessionUser user = SessionManager.getCurrentUser();
            if (user != null && authenticator.checkAuthorities(user.getId(), authRouter)) {
                isAllowed = true;
            }
        }
        if (isAllowed || !needAuth) {
            // 接口参数检查
            List<Integer> nonNullArgs = SpinContext.CHECKED_METHOD_PARAM.get(apiMethod.toGenericString());
            if (null == nonNullArgs) {
                nonNullArgs = new ArrayList<>();
                Annotation[][] annotations = apiMethod.getParameterAnnotations();
                for (int idx = 0; idx < annotations.length; ++idx) {
                    for (int j = 0; j < annotations[idx].length; ++j) {
                        if (annotations[idx][j] instanceof Needed) {
                            nonNullArgs.add(idx);
                        }
                    }
                }
                SpinContext.CHECKED_METHOD_PARAM.put(apiMethod.toGenericString(), nonNullArgs);
            }
            Object[] args = joinPoint.getArgs();
            for (Integer i : nonNullArgs) {
                if (null == args[i]) {
                    return RestfulResponse.error(ErrorCode.INVALID_PARAM);
                }
            }

            if (SpinContext.devMode && logger.isTraceEnabled()) {
                Parameter[] parameters = apiMethod.getParameters();
                logger.trace("Invoke method: {}", apiMethod.getName());
                for (int idx = 0; idx != parameters.length; ++idx) {
                    logger.trace("Parameter info: index[{}] name[{}], value[{}]", idx, parameters[idx].getName(), args[idx]);
                }
            }

            try {
                RestfulResponse r = (RestfulResponse) joinPoint.proceed();
                if (null == r) {
                    return RestfulResponse.ok();
                } else {
                    return r.setCodeAndMsg(ErrorCode.OK);
                }
            } catch (SimplifiedException e) {
                logger.info("Invoke api fail: [" + apiMethod.toGenericString() + "]");
                logger.trace("Exception: ", e);
                return RestfulResponse.error(e);
            } catch (Throwable throwable) {
                logger.error("Invoke api fail: [" + apiMethod.toGenericString() + "]", throwable);
                RestfulResponse response = RestfulResponse.error(ErrorCode.INTERNAL_ERROR);
                if (SpinContext.devMode) {
                    response.setMessage(throwable.getMessage());
                }
                return response;
            }
        } else
            return RestfulResponse.error(ErrorCode.ACCESS_DENINED);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
