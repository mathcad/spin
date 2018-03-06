package org.spin.web.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.data.core.IEntity;
import org.spin.data.util.EntityUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * Restful方法切面
 * <p>Created by xuweinan on 2018/3/6.</p>
 *
 * @author xuweinan
 */
@Aspect
@Component
public class RestfulMethodAspect implements Ordered {
    private static final Logger logger = LoggerFactory.getLogger(RestfulMethodAspect.class);

    @Pointcut("execution(public * *.*(..)) && @annotation(org.spin.web.annotation.RestfulMethod)")
    private void restfulMethod() {
    }

    @Around("restfulMethod()")
    public Object restfulAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object r = joinPoint.proceed();
        if (null == r) {
            return null;
        } else if (r instanceof IEntity<?>) {
            return EntityUtils.getDTO(r, 1);
        }
        return r;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
