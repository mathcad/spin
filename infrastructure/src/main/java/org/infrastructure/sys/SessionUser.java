package org.infrastructure.sys;

/**
 * 在线用户的实体接口
 *
 * @author xuweinan
 * @version V1.0
 */
public interface SessionUser {
    Long getId();

    String getUserName();

    String getPassword();

    String getRealName();

    void setSessionId(String sessionId);
}
