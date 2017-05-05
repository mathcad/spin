package org.spin.core;

import org.spin.enhance.util.SessionUtils;

import java.time.LocalDateTime;

/**
 * 在线用户接口
 * <p>
 * 用户实体应实现此接口<br>
 * 登录成功后，应通过<br>
 * {@link SessionUtils#setCurrentUser(SessionUser)}<br>
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

    LocalDateTime getLoginTime();

    String getSessionId();

    void setSessionId(String sessionId);
}
