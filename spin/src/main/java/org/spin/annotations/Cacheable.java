package org.spin.annotations;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记方法缓存
 * <p>Created by xuweinan on 2017/2/1.</p>
 *
 * @author xuweinan
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cacheable {

    @AliasFor("cacheable")
    boolean value() default true;

    /**
     * 是否缓存结果
     */
    @AliasFor("value")
    boolean cacheable() default true;

    /**
     * 是否启用严格模式
     * <p>参数的变化会视为不同的调用，使用不同的参数调用方法，会单独为其缓存结果</p>
     * <p>对参数频繁变化的方法，启用严格模式缓存会占用大量缓存空间，如果是内存缓存，甚至会导致内存耗尽</p>
     */
    boolean strict() default false;

    /**
     * 缓存的Key
     */
    String key() default "";
}
