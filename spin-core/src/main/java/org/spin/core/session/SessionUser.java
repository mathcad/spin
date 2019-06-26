package org.spin.core.session;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 在线用户接口
 * <p>
 * 用户实体应实现此接口,并且保证可以被正确地序列化<br>
 * 登录成功后，应通过
 * {@link SessionManager#setCurrentUser(SessionUser)}
 * 将用户实体存入session
 * </p>
 *
 * @author xuweinan
 * @version 1.0
 * @see Serializable
 * @deprecated 不建议再使用，有Session请自行选择相关框架
 */
@Deprecated
public interface SessionUser extends Serializable {
    Long getId();

    String getUserName();

    boolean isActive();

    LocalDateTime getLoginTime();

    Serializable getSessionId();

    void setSessionId(Serializable sessionId);
}
