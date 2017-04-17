package org.spin.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.annotations.Needed;
import org.spin.annotations.RestfulApi;
import org.spin.jpa.core.AbstractUser;
import org.spin.sys.EnvCache;
import org.spin.sys.ErrorCode;
import org.spin.sys.SessionUser;
import org.spin.sys.auth.Authenticator;
import org.spin.sys.auth.SecretManager;
import org.spin.throwable.SimplifiedException;
import org.spin.util.JSONUtils;
import org.spin.util.SessionUtils;
import org.spin.util.StringUtils;
import org.spin.web.RestfulResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * Restful切面
 * Created by xuweinan on 2016/10/2.
 *
 * @author xuweinan
 */
@Aspect
@Component
public class RestfulApiAspect {
    private static final Logger logger = LoggerFactory.getLogger(RestfulApiAspect.class);

    @Autowired(required = false)
    private Authenticator authenticator;

    @Autowired
    private SecretManager secretManager;

    @Pointcut("execution(org.spin.web.RestfulResponse *.*(..)) && @annotation(org.spin.annotations.RestfulApi)")
    private void restfulMethod() {
    }

    @Around("restfulMethod()")
    public Object checkAuthority(ProceedingJoinPoint joinPoint) {
        boolean isAllowed = false;
        Method apiMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RestfulApi anno = apiMethod.getAnnotation(RestfulApi.class);
        boolean needAuth = anno.auth();
        if (needAuth) {
            String authRouter = anno.authRouter();
            String token = (String) EnvCache.getLocalParam("token");
            if (StringUtils.isNotBlank(token)) {
                Object userId = checkAccess(token, authRouter);
                if (userId != null) {
                    SessionUtils.setCurrentUser(AbstractUser.ref((Long) userId));
//                    EnvCache.THREAD_LOCAL_PARAMETERS.get().put(SessionUtils.USER_SESSION_KEY, userId);
                    isAllowed = true;
                }
            } else {
                SessionUser user = SessionUtils.getCurrentUser();
                if (user != null && authenticator.checkAuthorities(user.getId(), authRouter)) {
                    isAllowed = true;
                }
            }
        }
        if (isAllowed || !needAuth) {
            // 接口参数检查
            List<Integer> nonNullArgs = EnvCache.CHECKED_METHOD_PARAM.get(apiMethod.toGenericString());
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
                EnvCache.CHECKED_METHOD_PARAM.put(apiMethod.toGenericString(), nonNullArgs);
            }
            Object[] args = joinPoint.getArgs();
            for (Integer i : nonNullArgs) {
                if (null == args[i]) {
                    return RestfulResponse.error(ErrorCode.INVALID_PARAM);
                }
            }

            if (EnvCache.devMode && logger.isTraceEnabled()) {
                Parameter[] parameters = apiMethod.getParameters();
                logger.trace("Invoke method: {0}", apiMethod.getName());
                for (int idx = 0; idx != parameters.length; ++idx) {
                    logger.trace("Parameter info: index[{0}] name[{1}], value[{2}]", idx, parameters[idx].getName(), JSONUtils.toJson(args[idx]));
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
                if (EnvCache.devMode) {
                    response.setMessage(throwable.getMessage());
                }
                return response;
            }
        } else
            return RestfulResponse.error(ErrorCode.ACCESS_DENINED);
    }

    private Object checkAccess(String token, String authRouter) {
        // token是否有效
        Object userId = secretManager.validateToken(token);
        // token对应用户是否拥有权限
        if (authenticator.checkAuthorities(userId, authRouter)) {
            return userId;
        }
        return null;
    }
}
