package org.spin.cloud.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识一个幂等的接口
 * <p>一个接口幂等，意味着对该接口的多次同一请求，将会得到同一结果</p>
 * <p>Created by xuweinan on 2020/7/13</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /**
     * 标识符 允许SpEL表达式. 该标识符必须唯一
     *
     * @return 标识符
     */
    String value() default "";
}
