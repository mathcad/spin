package org.spin.annotations;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义resetful接口，组合了{@code RequestMapping}，拦截并验证身份
 * <p>该注解修饰的方法，返回类型必须为String, CharSequence或者Object</p>
 * Created by xuweinan on 2016/10/2.
 *
 * @author xuweinan
 */
@RequestMapping
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

    /**
     * RequestMapping的名称
     */
    String name() default "";

    /**
     * RequestMapping的路径
     */
    @AliasFor("path")
    String[] value() default {};

    /**
     * RequestMapping的路径
     */
    @AliasFor("value")
    String[] path() default {};

    /**
     * 请求的方法
     */
    RequestMethod[] method() default {};

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
    String[] headers() default {};

    String[] consumes() default {};

    String[] produces() default {};
}