package org.spin.annotations;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义resetful接口，组合了 {@link RequestMapping}，{@link ResponseBody}与{@link Cacheable}，拦截并验证身份
 * <p>该注解修饰的方法，返回类型必须为RestfulResponse</p>
 * <p>Created by xuweinan on 2016/10/2.</p>
 *
 * @author xuweinan
 */
@RequestMapping
@ResponseBody
@Cacheable
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestfulApi {

    /**
     * 是否需要认证，默认true
     */
    boolean auth() default true;

    /**
     * 权限路径
     */
    String authRouter() default "";

    /**
     * RequestMapping的名称
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "name")
    String name() default "";

    /**
     * RequestMapping的路径
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "path")
    String[] value() default {};

    /**
     * RequestMapping的路径
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "path")
    String[] path() default {};

    /**
     * 请求的方法
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "method")
    RequestMethod[] method() default {};

    @AliasFor(annotation = RequestMapping.class, attribute = "params")
    String[] params() default {};

    /**
     * 请求的头部
     * <pre class="code">
     * &#064;RestfulApi(value = "/something", headers = "content-type=text/*")
     * </pre>
     * 将会匹配请求的Content-Type类型为"text/html", "text/plain", 这类的请求
     *
     * @see org.springframework.http.MediaType
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "headers")
    String[] headers() default {};

    @AliasFor(annotation = RequestMapping.class, attribute = "consumes")
    String[] consumes() default {};

    @AliasFor(annotation = RequestMapping.class, attribute = "produces")
    String[] produces() default {};

    /**
     * 是否启用缓存
     */
    @AliasFor(annotation = Cacheable.class, attribute = "cacheable")
    boolean cacheable() default true;

    /**
     * 是否启用严格模式
     * <p>参数的变化会视为不同的调用，使用不同的参数调用方法，会单独为其缓存结果</p>
     * <p>对参数频繁变化的方法，启用严格模式缓存会占用大量缓存空间，如果是内存缓存，甚至会导致内存耗尽</p>
     */
    @AliasFor(annotation = Cacheable.class, attribute = "strict")
    boolean strict() default false;

    /**
     * 缓存的Key
     */
    @AliasFor(annotation = Cacheable.class, attribute = "key")
    String key() default "";
}
