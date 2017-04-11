package org.spin.util;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.sys.EnvCache;
import org.spin.sys.SessionUser;
import org.spin.throwable.SimplifiedException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 全局Session操作
 * <p>如果未配置Shiro，则只能使用该类中最基本的getCurrentUser与setCurrentUser
 *
 * @author xuweinan
 * @version V1.0
 */
public abstract class SessionUtils {
    private static final Logger logger = LoggerFactory.getLogger(SessionUtils.class);

    /** 在未启用Shiro情况下，通过线程绑定当前用户 */
    private static final ThreadLocal<SessionUser> sessionUserContainer = new ThreadLocal<>();

    public static final String USER_SESSION_KEY = "user_session";

    private SessionUtils() {
    }

    /**
     * 获得当前session（默认不创建新Session）
     */
    public static Session getSession() {
        return getSession(false);
    }

    /**
     * 获得session
     *
     * @param autoCreate 是否自动创建
     */
    public static Session getSession(boolean autoCreate) {
        checkShiroSupport();
        Subject currentUser = SecurityUtils.getSubject();
        return currentUser.getSession(autoCreate);
    }

    /**
     * 得到Session中，当前登录用户
     */
    public static SessionUser getCurrentUser() {
        if (!EnvCache.shiroEnabled) {
            return sessionUserContainer.get();
        } else {
            try {
                Session session = getSession();
                if (session != null)
                    return (SessionUser) session.getAttribute(USER_SESSION_KEY);
                else
                    return null;
            } catch (Throwable t) {
                logger.info("当前会话未获得登录用户");
                return null;
            }
        }
    }

    /**
     * 返回当前线程的 request对象
     */
    public static HttpServletRequest getRequest() {
        checkShiroSupport();
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attr.getRequest();
    }

    /**
     * 返回session中的值
     */
    public static Object getSessionAttr(String key) {
        checkShiroSupport();
        Session sess = getSession(true);
        return sess == null ? null : sess.getAttribute(key);
    }

    /**
     * 设置session中的值
     */
    public static void setSessionAttr(String key, Object value) {
        checkShiroSupport();
        Session sess = getSession(true);
        sess.setAttribute(key, value);
    }

    /**
     * 删除session中的值，并返回
     */
    public static Object removeSessionAttr(String key) {
        checkShiroSupport();
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
    public static void removeAllSessionAttr() {
        checkShiroSupport();
        Session sess = getSession();
        if (sess != null && sess.getAttributeKeys() != null) {
            sess.getAttributeKeys().forEach(sess::removeAttribute);
        }
    }

    /**
     * 移除所有属性，除 attr 以外
     */
    public static void removeAllSessionAttrExc(String... attr) {
        checkShiroSupport();
        Session sess = getSession();
        List<String> a = attr == null ? new ArrayList<>() : Arrays.asList(attr);
        Optional.ofNullable(sess).filter(s -> s.getAttributeKeys() != null)
            .ifPresent(s -> s.getAttributeKeys().stream().filter(k -> !a.contains(k.toString())).forEach(s::removeAttribute));
    }

    /**
     * 更新session中的 user
     *
     * @param sessionUser 账户
     */
    public static void setCurrentUser(SessionUser sessionUser) {
        if (!EnvCache.shiroEnabled) {
            sessionUserContainer.set(sessionUser);
        } else {
            Session sess = getSession(true);
            if (null != sessionUser) {
                sessionUser.setSessionId(sess.getId().toString());
            }
            sess.setAttribute(USER_SESSION_KEY, sessionUser);
        }
    }

    /**
     * 登出session
     */
    public static void logout() {
        checkShiroSupport();
        setCurrentUser(null);
        SecurityUtils.getSubject().logout();
    }

    public static void setSessionUser(Session session, SessionUser sessionUser) {
        checkShiroSupport();
        session.setAttribute(USER_SESSION_KEY, sessionUser);
    }

    private static void checkShiroSupport() {
        if (!EnvCache.shiroEnabled) {
            throw new SimplifiedException("未启用Shiro，不支持session管理");
        }
    }
}
