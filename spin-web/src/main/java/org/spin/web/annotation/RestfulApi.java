package org.spin.web.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义resetful接口，组合了 {@link RequestMapping}与{@link ResponseBody}，拦截并验证身份
 * <p>该注解修饰的方法，返回类型必须为RestfulResponse</p>
 * <p>Created by xuweinan on 2016/10/2.</p>
 *
 * @author xuweinan
 */
@RequestMapping
@ResponseBody
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestfulApi {

    /**
     * 是否需要认证，默认true
     */
    boolean auth() default true;

    /**
     * 权限路径(RequestMapping的名称)
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "name")
    String authRouter() default "";

    /**
     * 权限路径(RequestMapping的名称)
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
}
