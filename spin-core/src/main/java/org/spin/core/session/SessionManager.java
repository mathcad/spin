package org.spin.core.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    private static final ThreadLocal<String> SESSION_ID_CONTAINER = new ThreadLocal<>();

    private static SessionDao sessionDao;

    public static final String USER_SESSION_KEY = "user_session";

    private SessionManager() {
    }

    /**
     * 获取当前线程绑定的用户Session，如果不存在，返回null
     *
     * @return 当前线程上的Session
     */
    public static Session getCurrentSession() {
        String sessId = SESSION_ID_CONTAINER.get();
        if (Objects.isNull(sessId)) {
            return null;
        }
        Session session = sessionDao.get(sessId);
        if (Objects.isNull(session)) {
            SimpleSession s = new SimpleSession();
            s.setId(sessId);
//            s.setAttribute(USER_SESSION_KEY, sessionUserContainer.get());
            sessionDao.save(s);
            session = s;
        }
        return session;
    }

    /**
     * 获取当前线程绑定的用户Session，如果不存在，返回null
     *
     * @return 当前线程上的Session
     */
    public static Session getCurrentSession(boolean requiredNew) {
        String sessId = SESSION_ID_CONTAINER.get();
        if (Objects.isNull(sessId)) {
            return null;
        }
        Session session = sessionDao.get(sessId);
        if (Objects.isNull(session) && requiredNew) {
            SimpleSession s = new SimpleSession();
            s.setId(sessId);
//            s.setAttribute(USER_SESSION_KEY, sessionUserContainer.get());
            sessionDao.save(s);
            session = s;
        }
        return session;
    }

    /**
     * 得到Session中，当前登录用户
     *
     * @return 当前登录用户
     */
    public static SessionUser getCurrentUser() {
        Session sess = getCurrentSession();
        return sess == null ? null : (SessionUser) sess.getAttribute(USER_SESSION_KEY);
    }

    /**
     * 返回session中的值
     *
     * @param key 属性名
     * @return 属性值
     */
    public static Object getSessionAttr(String key) {
        Session sess = getCurrentSession();
        return sess == null ? null : sess.getAttribute(key);
    }

    /**
     * 设置session中的值
     *
     * @param key   属性名
     * @param value 属性值
     */
    public static void setSessionAttr(String key, Object value) {
        Session sess = getCurrentSession();
        Assert.notNull(sess, "未能获取Session");
        sess.setAttribute(key, (Serializable) value);
        sessionDao.save(sess);
    }

    /**
     * 删除session中的值，并返回
     *
     * @param key 属性名称
     * @return 删除的属性值
     */
    public static Object removeSessionAttr(String key) {
        Session sess = getCurrentSession();
        if (sess == null)
            return null;
        Object r = sess.getAttribute(key);
        sess.removeAttribute(key);
        sessionDao.save(sess);
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
        sessionDao.save(sess);
    }

    /**
     * 移除所有属性，除attr 以外
     *
     * @param attr 保留的属性
     */
    public static void removeAllSessionAttrExc(String... attr) {
        Session sess = getCurrentSession();
        List<String> a = attr == null ? new ArrayList<>() : Arrays.asList(attr);
        Optional.ofNullable(sess).filter(s -> s.getAttributeKeys() != null)
            .ifPresent(s -> s.getAttributeKeys().stream().filter(k -> !a.contains(k.toString())).forEach(s::removeAttribute));
        sessionDao.save(sess);
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
            sessionDao.save(sess);
        }
    }

    /**
     * 登出session
     */
    public static void logout() {
        if (Objects.nonNull(SESSION_ID_CONTAINER.get())) {
            sessionDao.delete(SESSION_ID_CONTAINER.get());
            SESSION_ID_CONTAINER.set(null);
        }
    }

    public static void setSessionUser(Session session, SessionUser sessionUser) {
        session.setAttribute(USER_SESSION_KEY, sessionUser);
        sessionDao.save(session);
    }

    public static String getCurrentSessionId() {
        return SESSION_ID_CONTAINER.get();
    }

    public static void setCurrentSessionId(String sessionId) {
        SESSION_ID_CONTAINER.set(sessionId);
    }

    /**
     * 新的sessionid继承原有session
     *
     * @param oldSessionId 原SessionId
     * @param newSessionId 新SessionId
     */
    public static void extendSession(String oldSessionId, String newSessionId) {
        Session session = sessionDao.get(oldSessionId);
        if (Objects.nonNull(session)) {
            session.setId(newSessionId);
            sessionDao.save(session);
            sessionDao.delete(oldSessionId);
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
     * @return session是否存在
     */
    public static boolean containsSession(String sessionId) {
        return sessionDao.contains(sessionId);
    }

    /**
     * 移除session
     *
     * @param sessionId session id
     */
    public static void removeSession(String sessionId) {
        sessionDao.delete(sessionId);
    }

    /**
     * 移除session
     *
     * @param sessionIds session id集合
     */
    public static void removeSessions(Collection<String> sessionIds) {
        sessionIds.forEach(sessionDao::delete);
    }

    public static void setSessionDao(SessionDao sessionDao) {
        SessionManager.sessionDao = sessionDao;
    }
}


