package org.spin.enhance.shiro.realm;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.spin.core.SessionUser;
import org.spin.core.auth.Authenticator;
import org.spin.core.auth.RolePermission;
import org.spin.core.util.MethodUtils;
import org.spin.core.util.StringUtils;

/**
 * 基于用户标识符与密码的Realm
 *
 * @author xuweinan
 */
public class UsernamePasswordRealm extends AuthorizingRealm {
    private Authenticator<SessionUser> authenticator;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token != null && token instanceof UsernamePasswordToken;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) {
        String username = (String) authenticationToken.getPrincipal();
        if (null == authenticator)
            throw new AccountException("未配置认证功能");
        SessionUser user = authenticator.getSubject(username);
        if (user == null) {
            throw new UnknownAccountException("用户不存在");
        }
        if (!user.isActive()) {
            throw new DisabledAccountException("该用户已经被停用");
        }
        try {
            authenticator.preCheck(user);
        } catch (Exception e) {
            throw new AccountException(e.getMessage());
        }

        String password = null;
        String salt = null;
        try {
            password = MethodUtils.invokeMethod(user, "getPassword", null).toString();
            salt = MethodUtils.invokeMethod(user, "getSalt", null).toString();
        } catch (Exception e) {

        }
        return new SimpleAuthenticationInfo(user.getUserName(), password, StringUtils.isEmpty(salt) ? null : ByteSource.Util.bytes(salt), getName());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        if (null == authenticator)
            throw new AuthorizationException("未配置认证功能");
        String username = principalCollection.getPrimaryPrincipal().toString();
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        RolePermission rp = authenticator.getRolePermissionList(username);
        if (null != rp.getRoles())
            rp.getRoles().forEach(authorizationInfo::addRole);
        if (null != rp.getPermissions())
            rp.getPermissions().forEach(authorizationInfo::addStringPermission);
        return authorizationInfo;
    }
}
