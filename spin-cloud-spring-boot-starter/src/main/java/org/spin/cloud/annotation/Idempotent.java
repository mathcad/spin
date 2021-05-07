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

    /**
     * 是否允许重入, 关闭后, 重复请求将会返回错误(默认为true)
     *
     * @return 是/否
     */
    boolean reentrantable() default true;

    /**
     * 重复请求返回的错误信息
     *
     * @return 错误信息
     */
    String errorMessage() default "请勿重复请求";
}
