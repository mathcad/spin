package org.spin.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Created by xuweinan on 2017/9/13.</p>
 *
 * @author xuweinan
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestfulMethod {

    /**
     * 是否需要认证，默认true
     */
    boolean auth() default true;

    /**
     * 名称，默认为方法名
     */
    String value() default "";

    /**
     * 权限路径
     */
    String authRouter() default "";

    /**
     * 返回hibernate实体时，懒加载对象获取深度
     *
     * @return 深度，默认1
     */
    int fetchDepth() default 1;
}
