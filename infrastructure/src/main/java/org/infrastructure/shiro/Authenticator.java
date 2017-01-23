package org.infrastructure.shiro;

import org.infrastructure.jpa.core.AbstractUser;

/**
 * 自定义身份验证接口
 * <p>
 * Created by xuweinan on 2016/10/4.
 *
 * @author xuweinan
 */
public interface Authenticator {

    /**
     * 获取用户
     *
     * @param identity 用户标识符，具体由实现类定义
     */
    AbstractUser getSubject(Object identity);

    /**
     * 验证用户密码前的自定义校验，如验证用户类型等。
     * <p>验证不通过直接抛出异常即可</p>
     */
    void preCheck(AbstractUser user);

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
     */
    boolean authenticate(Object id, String password);

    /**
     * 返回权限验证结果
     *
     * @param id    用户ID
     * @param privs 权限列表
     */
    boolean checkAuthorities(Object id, Object[] privs);
}