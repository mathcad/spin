package org.spin.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.spin.util.SessionUtils;
import org.spin.util.StringUtils;
import org.spin.wx.wx.base.WxUserInfo;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * OAuth2认证shiro过滤器
 *
 * @author xuweinan
 */
public class WxAuthenticationFilter extends AuthenticatingFilter {
    private String authcCodeParam = "code";
    //服务器端登录成功/失败后重定向到的客户端地址
    private String failureUrl;
    private String completeUrl = "/wx/evpi";

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String credentials = httpRequest.getParameter(authcCodeParam);
        String principal = "";
        return new OAuth2Token(principal, credentials);
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        return SecurityUtils.getSubject().getPrincipal() instanceof WxUserInfo
                || (!isLoginRequest(request, response) && isPermissive(mappedValue));
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        String error = request.getParameter("error");
        String errorDescription = request.getParameter("error_description");
        if (!StringUtils.isEmpty(error)) {//如果服务端返回了错误
            WebUtils.issueRedirect(request, response, failureUrl + "?error=" + error + "error_description=" + errorDescription);
            return false;
        }
        Subject subject = getSubject(request, response);
        if (!(subject.getPrincipal() instanceof WxUserInfo) || !subject.isAuthenticated()) {
            if (StringUtils.isEmpty(request.getParameter(authcCodeParam)) || StringUtils.isEmpty(request.getParameter("state"))) {
                WebUtils.issueRedirect(request, response, failureUrl);
                return false;
            }
        }
        //执行父类里的登录逻辑，调用Subject.login登录
        return executeLogin(request, response);
    }

    //登录成功后的回调方法 重定向到成功页面
    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) throws Exception {
        SessionUtils.setSessionAttr("wxUserInfo", subject.getPrincipal());
        issueSuccessRedirect(request, response);
        return false;
    }

    //登录失败后的回调
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException ae, ServletRequest request, ServletResponse response) {
        Subject subject = getSubject(request, response);
        if (subject.isAuthenticated() || subject.isRemembered()) {
            try { //如果身份验证成功了 则重定向到成功页面
                issueSuccessRedirect(request, response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try { //登录失败时重定向到失败页面  
                WebUtils.issueRedirect(request, response, failureUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public String getFailureUrl() {
        return failureUrl;
    }

    public void setFailureUrl(String failureUrl) {
        this.failureUrl = failureUrl;
    }
}