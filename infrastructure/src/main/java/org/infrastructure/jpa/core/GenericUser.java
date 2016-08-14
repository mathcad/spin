package org.infrastructure.jpa.core;

import org.infrastructure.shiro.SessionUser;
import org.infrastructure.util.ReflectionUtils;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.lang.reflect.Field;

/**
 * 基础User类型
 *
 * @version V1.1
 */
@MappedSuperclass
public class GenericUser extends AbstractEntity implements SessionUser {
    private static final long serialVersionUID = 3167420402594165461L;

    @Column(nullable = false)
    private String loginName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String realName;

    @Override
    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    @Override
    public void setSessionId(String sessionId) {
    }

    public static GenericUser ref(Long id) {
        GenericUser aUser = new GenericUser();
        Field idField = ReflectionUtils.findField(aUser.getClass(), "id");
        ReflectionUtils.makeAccessible(idField);
        ReflectionUtils.setField(idField, aUser, id);
        return aUser;
    }
}
