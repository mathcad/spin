package org.spin.core.session;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;

/**
 * Session定义
 * <p>Created by xuweinan on 2017/10/17.</p>
 *
 * @author xuweinan
 */
public interface Session extends Serializable {

    Serializable getId();

    LocalDateTime getStartTimestamp();

    LocalDateTime getLastAccessTime();

    long getTimeout();

    void setTimeout(long maxIdleTimeInMillis);

    String getHost();

    void touch();

    void stop();

    Collection<Object> getAttributeKeys();

    Object getAttribute(Object key);

    void setAttribute(Object key, Object value);

    Object removeAttribute(Object key);

    boolean isValid();

    void validate();
}

