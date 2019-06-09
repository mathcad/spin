package org.spin.common.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * description 授权
 *
 * @author wangy QQ 837195190
 * <p>Created by wangy on 2019/3/14.</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Auth {

    /**
     * 是否需要认证，默认true
     *
     * @return 是否需要认证
     */
    boolean value() default true;

    /**
     * 授权资源名称
     *
     * @return 授权资源名称字符串
     */
    String name() default "";

    /**
     * 授权信息
     *
     * @return 所需权限列表
     */
    String[] permissions() default {};
}
