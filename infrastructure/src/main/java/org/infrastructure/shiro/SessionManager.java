package org.infrastructure.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.infrastructure.sys.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 全局Session实现类 基于shiro实现
 *
 * @author xuweinan
 * @version V1.0
 */
public class SessionManager {
    static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

    /**
     * 获得当前session（默认不创建新Session）
     */
    public Session getSession() {
        return getSession(false);
    }

    /**
     * 获得session
     *
     * @param autoCreate 是否自动创建
     */
    public Session getSession(boolean autoCreate) {
        Subject currentUser = SecurityUtils.getSubject();
        return currentUser.getSession(autoCreate);
    }

    /**
     * 得到Session中，当前登录用户
     */
    public <T extends SessionUser> T getCurrentUser() {
        try {
            Session session = getSession();
            if (session == null)
                return null;
            @SuppressWarnings("unchecked")
            T user = session.getAttribute(AppConstants.USER_SESSION_KEY) == null ? null
                    : (T) session.getAttribute(AppConstants.USER_SESSION_KEY);
            return user;
        } catch (Throwable t) {
            logger.info("sessionMgr 未获得 currentUser");
            return null;
        }
    }

    /**
     * 返回当前线程的 request对象
     */
    public HttpServletRequest getRequest() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attr.getRequest();
    }

    /**
     * 返回session中的值
     */
    public Object getSessionAttr(String key) {
        Session sess = getSession(true);
        return sess == null ? null : sess.getAttribute(key);
    }

    /**
     * 设置session中的值
     */
    public void setSessionAttr(String key, Object value) {
        Session sess = getSession(true);
        sess.setAttribute(key, value);
    }

    /**
     * 删除session中的值，并返回
     */
    public Object removeSessionAttr(String key) {
        Session sess = getSession();
        if (sess == null)
            return null;
        Object r = sess.getAttribute(key);
        sess.removeAttribute(key);
        return r;
    }

    /**
     * 移除所有属性
     */
    public void removeAllSessionAttr() {
        Session sess = getSession();
        if (sess != null && sess.getAttributeKeys() != null) {
            sess.getAttributeKeys().forEach(sess::removeAttribute);
        }
    }

    /**
     * 移除所有属性，除 attr 以外
     */
    public void removeAllSessionAttrExc(String... attr) {
        Session sess = getSession();
        List<String> a = attr == null ? new ArrayList<>() : Arrays.asList(attr);
        Optional.ofNullable(sess).filter(s -> s != null && s.getAttributeKeys() != null)
                .ifPresent(s -> s.getAttributeKeys().stream().filter(k -> !a.contains(k.toString())).forEach(s::removeAttribute));
    }

    /**
     * 更新session中的 user
     *
     * @param accInfo 账户
     */
    public void setCurrentUser(SessionUser accInfo) {
        Session sess = getSession(true);
        sess.setAttribute(AppConstants.USER_SESSION_KEY, accInfo);
    }
}
