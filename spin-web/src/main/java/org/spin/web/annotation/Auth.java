package org.spin.web.annotation;


import org.spin.web.AuthLevel;
import org.spin.web.ScopeType;

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
     * 认证级别，默认为仅认证
     *
     * @return 认证级别
     */
    AuthLevel value() default AuthLevel.AUTHENCATE;

    /**
     * 授权资源名称
     *
     * @return 授权资源名称字符串
     */
    String name() default "";

    /**
     * 授权信息
     *
     * @return 所需角色列表
     */
    String[] roles() default {};

    /**
     * 接口可见范围
     * <pre>
     *     OPEN        公开调用
     *     INTERNAL    内部调用，意味着该接口仅能由服务间相互调用，不允许由网关分发。
     *     OPEN_UNAUTH 公开调用，且内部调用时不验证用户身份(无论是否指定auth)
     * </pre>
     *
     * @return 接口可见范围
     * @see ScopeType
     */
    ScopeType scope() default ScopeType.OPEN;

    /**
     * 是否是开放接口, 默认不是
     *
     * @return 是否是开放接口
     */
    boolean openAuth() default false;
}
