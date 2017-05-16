package org.spin.enhance.shiro.session;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.SessionUser;
import org.spin.data.cache.Cache;
import org.spin.enhance.util.SessionUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 * session Session存储
 */
public class RedisShiroSessionDao extends AbstractSessionDAO {
    static Logger logger = LoggerFactory.getLogger(RedisShiroSessionDao.class.getName());
    private String sessionPrefix = "session";
    private Cache<Session> cache;
    private SessionListener sessListener;

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

    @Override
    public void delete(Session session) {
        try {
            if (this.sessListener != null) {
                try {
                    this.sessListener.beforeDeleteSession(session);
                    SessionUser user = (SessionUser) session.getAttribute(SessionUtils.USER_SESSION_KEY);
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
        String keys = sessionPrefix + "-*";
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
            super.assignSessionId(session, sessionPrefix + "-" + super.generateSessionId(session));
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

    public String getSessionPrefix() {
        return sessionPrefix;
    }

    public void setSessionPrefix(String sessionPrefix) {
        this.sessionPrefix = sessionPrefix;
    }

    /**
     * sess监听器
     */
    public void setSessListener(SessionListener sessListener) {
        this.sessListener = sessListener;
    }

    /**
     * session处理监听器
     */
    public interface SessionListener {
        void beforeUpdateSession(Session session);

        void beforeDeleteSession(Session session);
    }
}
