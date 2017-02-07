package org.spin.shiro.redis;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.spin.cache.Cache;
import org.spin.sys.SessionUser;
import org.spin.util.SessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

/**
 * redis Session存储
 */
public class RedisShiroSessionDao extends AbstractSessionDAO {
    static Logger logger = LoggerFactory.getLogger(RedisShiroSessionDao.class.getName());
    private String sessionprefix = "ss-";
    private Cache<Session> cache;

    Date lastSyncTime = null;

    @Override
    public void update(Session session) throws UnknownSessionException {
        try {
            if (this.sessListener != null) {
                try {
                    this.sessListener.beforeUpdateSession(session);
                } catch (Exception e) {
                    logger.error("", e);
                }
            }

            String sessionId = session.getId().toString();
            long timeout = session.getTimeout() / 1000;
            cache.put(sessionId, session);
            cache.expire(sessionId, timeout);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    /**
     * session处理监听器
     */
    public interface SessionListener {

        void beforeUpdateSession(Session session);

        void beforeDeleteSession(Session session);
    }

    SessionListener sessListener;

    /**
     * sess监听器
     */
    public void setSessListener(SessionListener sessListener) {
        this.sessListener = sessListener;
    }

    @Override
    public void delete(Session session) {
        try {
            if (this.sessListener != null) {
                try {
                    this.sessListener.beforeDeleteSession(session);
                    SessionUser user = session.getAttribute(SessionUtils.USER_SESSION_KEY) == null ? null : (SessionUser) session.getAttribute(SessionUtils.USER_SESSION_KEY);
                    if (user != null) {

                    }
                } catch (Exception e) {
                    logger.error("", e);
                }
            }

            cache.delete(session.getId().toString());
            logger.info("delete session:" + session.getId());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    @Override
    public Collection<Session> getActiveSessions() {
        String keys = sessionprefix + "*";
        Set<Session> list = null;
        try {
            list = cache.getValues(keys);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Serializable doCreate(Session session) {
        Serializable sessionId = session.getId();
        try {
            super.assignSessionId(session, sessionprefix + super.generateSessionId(session));
            update(session);
            sessionId = session.getId();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return sessionId;

    }

    @Override
    public Session doReadSession(Serializable sessionId) {
        Session session = null;
        try {
            session = cache.get(sessionId.toString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return session;
    }

    public Cache<Session> getCache() {
        return cache;
    }

    public void setCache(Cache<Session> cache) {
        this.cache = cache;
    }

    public String getSessionprefix() {
        return sessionprefix;
    }

    public void setSessionprefix(String sessionprefix) {
        this.sessionprefix = sessionprefix;
    }
}