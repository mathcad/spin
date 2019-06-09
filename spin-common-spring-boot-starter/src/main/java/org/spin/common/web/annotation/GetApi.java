package org.spin.common.web.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义resetful接口，组合了 {@link RequestMapping}({@link RequestMethod#GET})与{@link Auth}，拦截并验证身份
 * <p>Created by xuweinan on 2016/10/2.</p>
 *
 * @author xuweinan
 */
@RequestMapping(method = RequestMethod.GET)
@Auth
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GetApi {

    /**
     * 是否需要认证，默认true
     *
     * @return 是否需要认证
     */
    @AliasFor(annotation = Auth.class, attribute = "value")
    boolean auth() default true;

    /**
     * 授权资源名称
     *
     * @return 授权资源名称字符串
     */
    @AliasFor(annotation = Auth.class, attribute = "name")
    String authName() default "";


    /**
     * 授权信息
     *
     * @return 所需权限列表
     */
    @AliasFor(annotation = Auth.class, attribute = "permissions")
    String[] permissions() default {};

    /**
     * RequestMapping的名称
     *
     * @return 权限路径字符串
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "name")
    String name() default "";

    /**
     * RequestMapping的路径
     *
     * @return 资源访问路径
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "path")
    String[] value() default {};

    /**
     * RequestMapping的路径
     *
     * @return 资源访问路径
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "path")
    String[] path() default {};


    @AliasFor(annotation = RequestMapping.class, attribute = "params")
    String[] params() default {};

    /**
     * 请求的头部
     * <pre class="code">
     * &#064;RestfulApi(value = "/something", headers = "content-type=text/*")
     * </pre>
     * 将会匹配请求的Content-Type类型为"text/html", "text/plain", 这类的请求
     *
     * @return 请求Head部分
     * @see org.springframework.http.MediaType
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "headers")
    String[] headers() default {};

    @AliasFor(annotation = RequestMapping.class, attribute = "consumes")
    String[] consumes() default {};

    @AliasFor(annotation = RequestMapping.class, attribute = "produces")
    String[] produces() default {};
}
