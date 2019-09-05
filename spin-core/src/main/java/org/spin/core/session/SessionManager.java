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
 * @version 1.0
 * @deprecated 不建议再使用，有Session请自行选择相关框架
 */
@Deprecated
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
        // 未启用Session管理
        if (null == sessionDao) {
            return null;
        }
        String sessId = SESSION_ID_CONTAINER.get();
        if (Objects.isNull(sessId)) {
            return null;
        }
        Session session = sessionDao.get(sessId);
        if (Objects.isNull(session)) {
            session = sessionDao.createSession(sessId);
        }
        return session;
    }

    /**
     * 获取当前线程绑定的用户Session，如果不存在，返回null
     *
     * @param requiredNew 当前不存在session时，创建一个新的session
     * @return 当前线程上的Session
     */
    public static Session getCurrentSession(boolean requiredNew) {
        // 未启用Session管理
        if (null == sessionDao) {
            return null;
        }
        String sessId = SESSION_ID_CONTAINER.get();
        if (Objects.isNull(sessId)) {
            return null;
        }
        Session session = sessionDao.get(sessId);
        if (Objects.isNull(session) && requiredNew) {
            session = sessionDao.createSession(sessId);
        }
        return session;
    }

    /**
     * 得到Session中，当前登录用户
     *
     * @return 当前登录用户
     */
    public static SessionUser getCurrentUser() {
        // 未启用Session管理
        if (null == sessionDao) {
            return null;
        }
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
        // 未启用Session管理
        if (null == sessionDao) {
            return null;
        }
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
        // 未启用Session管理
        if (null == sessionDao) {
            return;
        }
        Session session = getCurrentSession();
        Assert.notNull(session, "未能获取Session");
        session.setAttribute(key, (Serializable) value);
        sessionDao.save(session);
    }

    /**
     * 删除session中的值，并返回
     *
     * @param key 属性名称
     * @return 删除的属性值
     */
    public static Object removeSessionAttr(String key) {
        // 未启用Session管理
        if (null == sessionDao) {
            return null;
        }
        Session session = getCurrentSession();
        if (session == null)
            return null;
        Object r = session.getAttribute(key);
        session.removeAttribute(key);
        sessionDao.save(session);
        return r;
    }

    /**
     * 移除所有属性
     */
    public static void removeAllSessionAttr() {
        // 未启用Session管理
        if (null == sessionDao) {
            return;
        }
        Session session = getCurrentSession();
        if (session != null && session.getAttributeKeys() != null) {
            session.getAttributeKeys().forEach(session::removeAttribute);
        }
        sessionDao.save(session);
    }

    /**
     * 移除所有属性，除attr以外
     *
     * @param attr 保留的属性
     */
    public static void removeAllSessionAttrExc(String... attr) {
        // 未启用Session管理
        if (null == sessionDao) {
            return;
        }
        Session session = getCurrentSession();
        List<String> a = attr == null ? new ArrayList<>() : Arrays.asList(attr);
        Optional.ofNullable(session).filter(s -> s.getAttributeKeys() != null)
            .ifPresent(s -> s.getAttributeKeys().stream().filter(k -> !a.contains(k.toString())).forEach(s::removeAttribute));
        sessionDao.save(session);
    }

    /**
     * 更新session中的 user
     *
     * @param sessionUser 账户
     */
    public static void setCurrentUser(SessionUser sessionUser) {
        // 未启用Session管理
        if (null == sessionDao) {
            return;
        }
        Session session = getCurrentSession();
        if (Objects.nonNull(session)) {
            sessionUser.setSessionId(getCurrentSessionId());
            session.setAttribute(USER_SESSION_KEY, sessionUser);
            sessionDao.save(session);
        }
    }

    /**
     * 登出session
     */
    public static void logout() {
        // 未启用Session管理
        if (null == sessionDao) {
            return;
        }
        if (Objects.nonNull(SESSION_ID_CONTAINER.get())) {
            sessionDao.delete(SESSION_ID_CONTAINER.get());
            SESSION_ID_CONTAINER.set(null);
        }
    }

    public static void setSessionUser(Session session, SessionUser sessionUser) {
        // 未启用Session管理
        if (null == sessionDao) {
            return;
        }
        session.setAttribute(USER_SESSION_KEY, sessionUser);
        sessionDao.save(session);
    }

    public static String getCurrentSessionId() {
        // 未启用Session管理
        if (null == sessionDao) {
            return null;
        }
        return SESSION_ID_CONTAINER.get();
    }

    public static void setCurrentSessionId(String sessionId) {
        // 未启用Session管理
        if (null == sessionDao) {
            return;
        }
        SESSION_ID_CONTAINER.set(sessionId);
    }

    /**
     * 新的sessionid继承原有session
     *
     * @param oldSessionId 原SessionId
     * @param newSessionId 新SessionId
     */
    public static void extendSession(String oldSessionId, String newSessionId) {
        // 未启用Session管理
        if (null == sessionDao) {
            return;
        }
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
        // 未启用Session管理
        if (null == sessionDao) {
            return false;
        }
        return sessionDao.contains(sessionId);
    }

    /**
     * 移除session
     *
     * @param sessionId session id
     */
    public static void removeSession(String sessionId) {
        // 未启用Session管理
        if (null == sessionDao) {
            return;
        }
        sessionDao.delete(sessionId);
    }

    /**
     * 移除session
     *
     * @param sessionIds session id集合
     */
    public static void removeSessions(Collection<String> sessionIds) {
        // 未启用Session管理
        if (null == sessionDao) {
            return;
        }
        sessionIds.forEach(sessionDao::delete);
    }

    public static void setSessionDao(SessionDao sessionDao) {
        SessionManager.sessionDao = sessionDao;
    }

    /**
     * 是否启用Session管理
     *
     * @return 是否启用
     */
    public static boolean isSessionManagerEnabled() {
        return null != sessionDao;
    }
}


