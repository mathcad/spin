package org.spin.core.session;

import java.io.Serializable;

/**
 * Session存储
 * <p>Created by xuweinan on 2017/10/26.</p>
 *
 * @author xuweinan
 */
public interface SessionDao {

    /**
     * 存储session
     *
     * @param session session对象
     */
    void save(Session session);

    /**
     * 根据sessionId获取session，如果不存在返回null
     *
     * @param sessionId 指定的sessionId
     * @return Session对象
     */
    Session get(Serializable sessionId);

    /**
     * 删除session
     *
     * @param sessionId 指定的sessionId
     */
    void delete(Serializable sessionId);

    /**
     * 判断指定ID的session是否存在
     *
     * @param sessionId id
     * @return 是否存在
     */
    boolean contains(Serializable sessionId);

    /**
     * 返回当前有效的session数量
     *
     * @return 存活的session数量
     */
    Long validCount();
}
