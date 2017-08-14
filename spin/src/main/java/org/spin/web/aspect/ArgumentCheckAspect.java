package org.spin.web.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * 方法参数检查切面
 * <p>Created by xuweinan on 2017/5/6.</p>
 *
 * @author xuweinan
 */
//@Aspect
//@Component
public class ArgumentCheckAspect implements Ordered {

    @Pointcut("execution(org.spin.web.RestfulResponse *.*(..)) && @annotation(org.spin.web.annotation.RestfulApi)")
    private void checkMethod() {
    }

    // TODO 参数检查切面
    @Before("checkMethod()")
    public Object checkerAround(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            return throwable;
        }
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
