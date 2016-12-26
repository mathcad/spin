package org.infrastructure.aspect;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.infrastructure.annotations.Needed;
import org.infrastructure.annotations.RestfulApi;
import org.infrastructure.jpa.core.AbstractUser;
import org.infrastructure.shiro.Authenticator;
import org.infrastructure.sys.EnvCache;
import org.infrastructure.sys.TokenKeyManager;
import org.infrastructure.throwable.SimplifiedException;
import org.infrastructure.util.SessionUtils;
import org.infrastructure.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
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

    @Autowired
    TokenKeyManager tokenKeyManager;

    @Autowired
    Authenticator authenticator;

    @Pointcut("@annotation(org.infrastructure.annotations.RestfulApi)")
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
                String token = EnvCache.THREAD_LOCAL_PARAMETERS.get() == null ? null : (String) EnvCache.THREAD_LOCAL_PARAMETERS.get().get("token");
                Object userId = checkAccess(token, authorities);
                if (userId != null) {
                    SessionUtils.setCurrentUser(AbstractUser.ref((Long) userId));
//                    EnvCache.THREAD_LOCAL_PARAMETERS.get().put(SessionUtils.USER_SESSION_KEY, userId);
                    isAllowed = true;
                }
            }
        } else {
            SessionUtils.setCurrentUser(AbstractUser.ref(1L));
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

            try {
                Object r = joinPoint.proceed();
                if (!(r instanceof String))
                    throw new SimplifiedException("RestfulApi接口需要返回String的结果");
                return "{\"code\": 200, \"des\": \"OK\"" + (StringUtils.isNotEmpty((String) r) ? ", \"data\": " + r : "") + "}";
            } catch (SimplifiedException e) {
                logger.info("Invoke api fail: [" + apiMethod.toGenericString() + "]");
                return "{\"code\": 500, \"des\": \"" + e.getMessage() + "\"}";
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
        Object userId = tokenKeyManager.validateToken(token);
        // token对应用户是否拥有权限
        if (authenticator.checkAuthorities(userId, authorities))
            return userId;
        return null;
    }
}