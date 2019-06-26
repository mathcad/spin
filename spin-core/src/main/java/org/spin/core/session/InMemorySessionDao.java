package org.spin.core.session;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/10/25</p>
 *
 * @author xuweinan
 * @version 1.0
 * @deprecated 不建议再使用，有Session请自行选择相关框架
 */
@Deprecated
public class InMemorySessionDao implements SessionDao {

    /**
     * Session容器
     */
    private final Map<Serializable, Session> allSessions = new ConcurrentHashMap<>(256);

    public InMemorySessionDao() {
        SessionManager.setSessionDao(this);
    }

    @Override
    public Session createSession(Serializable sessionId) {
        SimpleSession session = new SimpleSession();
        session.setId(sessionId);
        allSessions.put(session.getId(), session);
        return session;
    }

    @Override
    public void save(Session session) {
        if (null != session) {
            allSessions.put(session.getId(), session);
        }
    }

    @Override
    public Session get(Serializable sessionId) {
        return null != sessionId ? allSessions.get(sessionId) : null;
    }

    @Override
    public void delete(Serializable sessionId) {
        if (null != sessionId) {
            allSessions.remove(sessionId);
        }
    }

    @Override
    public boolean contains(Serializable sessionId) {
        return allSessions.containsKey(sessionId);
    }

    @Override
    public Long validCount() {
        return allSessions.values().stream().filter(Session::isValid).count();
    }

    @Override
    public int sessionCount() {
        return allSessions.size();
    }

    @Override
    public void clearExpiredSession() {

    }

}
