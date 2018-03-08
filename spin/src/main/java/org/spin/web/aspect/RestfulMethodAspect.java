package org.spin.web.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.spin.data.core.IEntity;
import org.spin.data.util.EntityUtils;
import org.spin.web.annotation.RestfulMethod;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Restful方法切面
 * <p>Created by xuweinan on 2018/3/6.</p>
 *
 * @author xuweinan
 */
@Aspect
@Component
public class RestfulMethodAspect implements Ordered {

    @Pointcut("execution(public * *.*(..)) && @annotation(org.spin.web.annotation.RestfulMethod)")
    private void restfulMethod() {
        // no content
    }

    @Around("restfulMethod()")
    public Object restfulAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object r = joinPoint.proceed();
        Method rMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RestfulMethod anno = rMethod.getAnnotation(RestfulMethod.class);
        if (null == r) {
            return null;
        } else if (r instanceof IEntity<?>) {
            return EntityUtils.getDTO(r, anno.fetchDepth());
        }
        return r;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
