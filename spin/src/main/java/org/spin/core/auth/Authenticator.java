package org.spin.core.auth;

import java.time.LocalDateTime;

/**
 * 自定义身份验证接口
 * <p>
 * Created by xuweinan on 2016/10/4.
 *
 * @param <U> 用户类型
 * @author xuweinan
 */
public interface Authenticator<U> {

    /**
     * 获取用户
     *
     * @param identity 用户标识符，具体由实现类定义
     */
    U getSubject(Object identity);

    /**
     * 验证用户密码前的自定义校验，如验证用户类型等。
     * <p>验证不通过直接抛出异常即可</p>
     * 默认什么也不做
     */
    default void preCheck(U user) {
    }

    /**
     * 获取角色与权限信息
     *
     * @param identity 用户标识符
     */
    RolePermission getRolePermissionList(Object identity);

    /**
     * 返回身份验证结果
     *
     * @param id       用户ID
     * @param password 用户密码
     * @return 是否通过身份验证
     */
    boolean authenticate(Object id, String password);

    /**
     * 返回权限验证结果
     * <p>默认没有控制，直接返回true</p>
     *
     * @param id         用户ID
     * @param authRouter 权限路径
     * @return 是否通过验证
     */
    default boolean checkAuthorities(Object id, String authRouter) {
        return true;
    }

    /**
     * 记录访问日志
     * <p>默认不记录日志</p>
     *
     * @param subject    访问人
     * @param accessTime 访问时间
     * @param msg        日志
     */
    default void logAccess(Object subject, LocalDateTime accessTime, String msg) {
    }
}
