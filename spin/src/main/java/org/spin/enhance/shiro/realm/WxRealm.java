package org.spin.enhance.shiro.realm;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.spin.enhance.shiro.OAuth2Token;
import org.spin.wx.WxHelper;
import org.spin.wx.base.WxUserInfo;

/**
 * 微信认证域
 *
 * @author xuweinan
 */
public class WxRealm extends AuthorizingRealm {
    private String clientId;
    private String clientSecret;
    private String accessTokenUrl;
    private String userInfoUrl;
    private String redirectUrl;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token != null && token instanceof OAuth2Token;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        OAuth2Token oAuth2Token = (OAuth2Token) token;
        String code = oAuth2Token.getCredentials();
        WxUserInfo principal = extractPrincipal(code);
        return new SimpleAuthenticationInfo(principal, code, getName());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return new SimpleAuthorizationInfo();
    }

    private WxUserInfo extractPrincipal(String code) {
        try {
            return WxHelper.getUserInfo(code);
        } catch (Exception e) {
            throw new AuthenticationException("Can not verify the the user info");
        }
    }
}
