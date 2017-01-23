package org.infrastructure.sys;

import java.util.Date;

/**
 * 在线用户接口
 * <p>
 * 用户实体应实现此接口<br/>
 * 登录成功后，应通过<br/>
 * {@link org.infrastructure.util.SessionUtils#setCurrentUser(SessionUser)}<br/>
 * 将用户实体存入session
 * </p>
 *
 * @author xuweinan
 * @version V1.0
 */
public interface SessionUser {
    Long getId();

    String getUserName();

    boolean isActive();

    Date getLoginTime();

    String getSessionId();

    void setSessionId(String sessionId);
}