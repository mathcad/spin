package org.spin.web.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.spin.core.throwable.SimplifiedException;
import org.spin.data.core.IEntity;
import org.spin.data.util.EntityUtils;
import org.spin.web.annotation.LazyLoadDepth;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 懒加载实体处理切面
 * <p>Created by xuweinan on 2018/3/6.</p>
 *
 * @author xuweinan
 */
@Aspect
@Component
public class LazyLoadAspect implements Ordered {

    @Pointcut("execution(public * *.*(..)) && @annotation(org.spin.web.annotation.LazyLoadDepth)")
    private void lazyLoadMethod() {
        // no content
    }

    @Around("lazyLoadMethod()")
    public Object lazyLoadAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object r = joinPoint.proceed();
        Method rMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        LazyLoadDepth anno = rMethod.getAnnotation(LazyLoadDepth.class);
        if (null == r) {
            return null;
        } else if (r instanceof IEntity<?>) {
            if (anno.byArg() > 0) {
                int idx;
                try {
                    idx = (int) joinPoint.getArgs()[anno.byArg()];
                } catch (Exception e) {
                    throw new SimplifiedException("指定懒加载深度的参数必须为整型");
                }
                return EntityUtils.getDTO(r, idx);
            } else {
                return EntityUtils.getDTO(r, anno.value());
            }
        }
        return r;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
