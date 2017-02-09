package org.spin.aspect;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.ShiroException;
import org.apache.shiro.subject.Subject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.spin.annotations.Needed;
import org.spin.annotations.RestfulApi;
import org.spin.jpa.core.AbstractUser;
import org.spin.shiro.Authenticator;
import org.spin.sys.EnvCache;
import org.spin.sys.TokenKeyManager;
import org.spin.throwable.SimplifiedException;
import org.spin.util.JSONUtils;
import org.spin.util.SessionUtils;
import org.spin.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuweinan on 2016/10/2.
 *
 * @author xuweinan
 */
@Aspect
@Component
public class RestfulApiAspect {
    private static final Logger logger = LoggerFactory.getLogger(RestfulApiAspect.class);

    @Autowired(required = false)
    Authenticator authenticator = null;

    @Pointcut("@annotation(org.spin.annotations.RestfulApi)")
    private void restfulMethod() {
    }

    @Around("restfulMethod()")
    public Object checkAuthority(ProceedingJoinPoint joinPoint) {
        boolean isAllowed = false;
        Method apiMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RestfulApi anno = apiMethod.getAnnotation(RestfulApi.class);
        boolean needAuth = anno.auth();
        if (needAuth) {
            String[] authorities = anno.authorities();
            Subject user = SecurityUtils.getSubject();
            if (user != null && user.isAuthenticated() && user.isPermittedAll(authorities))
                isAllowed = true;
            else {
                String token = (String) EnvCache.getLocalParam("token");
                Object userId = checkAccess(token, authorities);
                if (userId != null) {
                    SessionUtils.setCurrentUser(AbstractUser.ref((Long) userId));
//                    EnvCache.THREAD_LOCAL_PARAMETERS.get().put(SessionUtils.USER_SESSION_KEY, userId);
                    isAllowed = true;
                }
            }
        } else {
            try {
                SessionUtils.setCurrentUser(AbstractUser.ref(1L));
            } catch (ShiroException e) {
                logger.warn("Shiro未配置，不能启用Session支持");
                if (EnvCache.devMode)
                    logger.trace("Exception:", e);
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
                        if (annotations[idx][j] instanceof Needed)
                            nonNullArgs.add(idx);
                    }
                }
                EnvCache.CHECKED_METHOD_PARAM.put(apiMethod.toGenericString(), nonNullArgs);
            }
            Object[] args = joinPoint.getArgs();
            for (Integer i : nonNullArgs) {
                if (null == args[i])
                    return "{\"code\": 412, \"des\": \"请求参数不完整\"}";
            }

            if (EnvCache.devMode && logger.isTraceEnabled()) {
                Parameter[] parameters = apiMethod.getParameters();
                logger.trace("Invoke method: {0}", apiMethod.getName());
                for (int idx = 0; idx != parameters.length; ++idx) {
                    logger.trace("Parameter info: index[{0}] name[{1}], value[{2}]", idx, parameters[idx].getName(), JSONUtils.toJson(args[idx]));
                }
            }

            String returnType = apiMethod.getReturnType().getName();
            if (!(returnType.equals(String.class.getName())
                    || returnType.equals(Object.class.getName())
                    || returnType.equals(CharSequence.class.getName())))
                throw new SimplifiedException("RestfulApi接口的返回类型错误，必须为String, CharSequence或者Object");
            try {
                Object r = joinPoint.proceed();
                String content = r instanceof CharSequence ? r.toString() : JSONUtils.toJson(r);
                return "{\"code\": 200, \"des\": \"OK\"" + (StringUtils.isNotEmpty(content) ? ", \"data\": " + content : "") + "}";
            } catch (SimplifiedException e) {
                logger.info("Invoke api fail: [" + apiMethod.toGenericString() + "]");
                logger.trace("Exception: ", e);
                int code = e.getExceptionType().getValue() > 400 ? e.getExceptionType().getValue() : 500;
                return StringUtils.format("{\"code\": {0}, \"des\": \"{1}\"}", code, e.getMessage());
            } catch (Throwable throwable) {
                logger.error("Invoke api fail: [" + apiMethod.toGenericString() + "]", throwable);
                if (EnvCache.devMode)
                    return "{\"code\": 500, \"des\": \"" + throwable.getMessage() + "\"}";
                return "{\"code\": 500, \"des\": \"服务端内部错误\"}";
            }
        } else
            return "{\"code\": 401, \"des\": \"未授权的访问\"}";
    }

    private Object checkAccess(String token, String[] authorities) {
        // token是否有效
        Object userId = TokenKeyManager.validateToken(token);
        // token对应用户是否拥有权限
        if (authenticator.checkAuthorities(userId, authorities))
            return userId;
        return null;
    }
}