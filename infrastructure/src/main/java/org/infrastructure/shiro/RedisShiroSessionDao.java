package org.infrastructure.shiro;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.infrastructure.redis.ICached;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * redis Session存储
 *
 * @author xuweinan
 * @version V1.0
 */
public class RedisShiroSessionDao extends AbstractSessionDAO {
	static Logger logger = LoggerFactory.getLogger(RedisShiroSessionDao.class.getName());

	private String sessionprefix = "ss-";
	private ICached<String, Session> cached;

	public RedisShiroSessionDao() {
	}

	Date lastSyncTime = null;

	@Override
	public void update(Session session) throws UnknownSessionException {
		try {
			// 未同步或者10s前同步的，执行同步（防止频繁同步）
			// if(lastSyncTime==null ||
			// DateUtils.addSeconds(lastSyncTime,10).before(new Date()){
			// }
			String sessionId = session.getId().toString();
			long timeout = session.getTimeout() / 1000;
			cached.put(sessionId, session);
			cached.setExpire(sessionId, timeout);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	@Override
	public void delete(Session session) {
		try {
			cached.deleteCached(session.getId().toString());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	@Override
	public Collection<Session> getActiveSessions() {
		String keyPattern = sessionprefix + "*";
		Set<Session> sessions = null;
		try {
			Set<String> keys = cached.getKeys(keyPattern);
			if (keys == null || keys.size() < 1)
				return null;
			sessions = new HashSet<Session>();
			for (String key : keys) {
				sessions.add(cached.get(key));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return sessions;
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
			session = (Session) cached.get(sessionId.toString());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return session;
	}

	public ICached<String, Session> getCached() {
		return cached;
	}

	public void setCached(ICached<String, Session> cached) {
		this.cached = cached;
	}

	public String getSessionprefix() {
		return sessionprefix;
	}

	public void setSessionprefix(String sessionprefix) {
		this.sessionprefix = sessionprefix;
	}

}
