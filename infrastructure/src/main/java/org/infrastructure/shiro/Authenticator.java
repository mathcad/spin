package org.infrastructure.shiro;

/**
 * 自定义身份验证接口
 * <p>
 * Created by xuweinan on 2016/10/4.
 *
 * @author xuweinan
 */
public interface Authenticator {

    /**
     * 返回身份验证结果
     *
     * @param id       用户ID
     * @param password 用户密码
     */
    boolean authenticate(Object id, String password);

    boolean checkAuthorities(Object id, Object[] privs);
}
