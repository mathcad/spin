package org.spin.core.session;

import java.io.Serializable;

/**
 * Session存储
 * <p>Created by xuweinan on 2017/10/26.</p>
 *
 * @author xuweinan
 */
public interface SessionDao {
    void save(Session session);

    Session get(Serializable sessionId);

    void delete(Serializable sessionId);

    Long validCount();
}
