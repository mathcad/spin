package org.spin.core.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局Session管理器
 *
 * @author xuweinan
 * @version V1.0
 */
public abstract class SessionManager {
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

    /**
     * 当前线程SessionId
     */
    private static final ThreadLocal<String> sessionIdContainer = new ThreadLocal<>();

    /**
     * Session容器
     */
    private static final Map<String, Session> ALL_SESSIONS = new ConcurrentHashMap<>();

    public static final String USER_SESSION_KEY = "user_session";

    private SessionManager() {
    }

    public static Session getCurrentSession() {
        String sessId = sessionIdContainer.get();
        if (Objects.isNull(sessId)) {
            return null;
        }
        Session session = ALL_SESSIONS.get(sessId);
        if (Objects.isNull(session)) {
            SimpleSession s = new SimpleSession();
            s.setId(sessionIdContainer.get());
//            s.setAttribute(USER_SESSION_KEY, sessionUserContainer.get());
            ALL_SESSIONS.put(sessId, s);
            session = s;
        }
        return session;
    }

    /**
     * 得到Session中，当前登录用户
     */
    public static SessionUser getCurrentUser() {
        Session sess = getCurrentSession();
        return sess == null ? null : (SessionUser) sess.getAttribute(USER_SESSION_KEY);
    }

    /**
     * 返回session中的值
     */
    public static Object getSessionAttr(String key) {
        Session sess = getCurrentSession();
        return sess == null ? null : sess.getAttribute(key);
    }

    /**
     * 设置session中的值
     */
    public static void setSessionAttr(String key, Object value) {
        Session sess = getCurrentSession();
        Assert.notNull(sess, "未能获取Session");
        sess.setAttribute(key, value);
    }

    /**
     * 删除session中的值，并返回
     */
    public static Object removeSessionAttr(String key) {
        Session sess = getCurrentSession();
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
        Session sess = getCurrentSession();
        if (sess != null && sess.getAttributeKeys() != null) {
            sess.getAttributeKeys().forEach(sess::removeAttribute);
        }
    }

    /**
     * 移除所有属性，除 attr 以外
     */
    public static void removeAllSessionAttrExc(String... attr) {
        Session sess = getCurrentSession();
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
        Session sess = getCurrentSession();
        if (Objects.nonNull(sess)) {
            sessionUser.setSessionId(getCurrentSessionId());
            sess.setAttribute(USER_SESSION_KEY, sessionUser);
        }
    }

    /**
     * 登出session
     */
    public static void logout() {
        if (Objects.nonNull(sessionIdContainer.get())) {
            ALL_SESSIONS.remove(sessionIdContainer.get());
            sessionIdContainer.set(null);
        }
    }

    public static void setSessionUser(Session session, SessionUser sessionUser) {
        session.setAttribute(USER_SESSION_KEY, sessionUser);
    }

    public static String getCurrentSessionId() {
        return sessionIdContainer.get();
    }

    public static void setCurrentSessionId(String sessionId) {
        sessionIdContainer.set(sessionId);
    }

    /**
     * 新的sessionid继承原有session
     */
    public static void extendSession(String oldSessionId, String newSessionId) {
        Session session = ALL_SESSIONS.get(oldSessionId);
        if (Objects.nonNull(session)) {
            ALL_SESSIONS.put(newSessionId, session);
            ALL_SESSIONS.remove(oldSessionId);
        }
        setCurrentSessionId(newSessionId);
        SessionUser user = getCurrentUser();
        if (Objects.nonNull(user)) {
            user.setSessionId(newSessionId);
        }
    }

    /**
     * 判断Session是否存在
     *
     * @param sessionId session id
     */
    public static boolean containsSession(String sessionId) {
        return ALL_SESSIONS.containsKey(sessionId);
    }

    /**
     * 移除session
     *
     * @param sessionId session id
     */
    public static void removeSession(String sessionId) {
        ALL_SESSIONS.remove(sessionId);
    }

    /**
     * 移除session
     *
     * @param sessionIds session id集合
     */
    public static void removeSessions(Collection<String> sessionIds) {
        sessionIds.forEach(ALL_SESSIONS::remove);
    }
}


