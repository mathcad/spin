package org.spin.enhance.shiro.filter;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.spin.core.SessionUser;
import org.spin.core.auth.Authenticator;
import org.spin.core.util.SessionUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 自定义登录处理
 * Created by xuweinan on 2016/9/27.
 *
 * @author xuweinan
 */
public class EnhancedAuthenticationFilter extends FormAuthenticationFilter {
    private Authenticator authenticator;

    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) throws Exception {
        SessionUser user = (SessionUser) authenticator.getSubject(token.getPrincipal());
        SessionUtils.setSessionUser(subject.getSession(), user);
        authenticator.logAccess(user, LocalDateTime.now(), "登录成功");
        return super.onLoginSuccess(token, subject, request, response);
    }

    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }
}
