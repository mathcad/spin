package org.infrastructure.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义resetful接口，拦截并验证身份
 * <p>
 * Created by xuweinan on 2016/10/2.
 *
 * @author xuweinan
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestfulApi {

    /**
     * 是否需要认证，默认true
     */
    boolean auth() default true;

    /**
     * 需要的权限列表
     */
    String[] authorities() default {};
}