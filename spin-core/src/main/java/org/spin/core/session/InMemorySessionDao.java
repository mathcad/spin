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
 */
public class InMemorySessionDao implements SessionDao {

    /**
     * Session容器
     */
    private final Map<Serializable, Session> allSessions = new ConcurrentHashMap<>();

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
        return null;
    }

}