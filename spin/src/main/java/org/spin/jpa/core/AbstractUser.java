package org.spin.jpa.core;


import org.spin.core.SessionUser;
import org.spin.core.util.DigestUtils;
import org.spin.core.util.RandomStringUtils;
import org.spin.core.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.time.LocalDateTime;

/**
 * 用户的父类，定义了用户的通用属性
 * <p>Created by xuweinan on 2016/10/5.</p>
 *
 * @author xuweinan
 */
@MappedSuperclass
public abstract class AbstractUser extends AbstractEntity implements SessionUser {
    private static final long serialVersionUID = 4486949049542319147L;

    /**
     * 用户名
     */
    @Column(length = 32)
    private String userName;

    /**
     * 密码
     */
    @Column(length = 64)
    private String password;

    /**
     * 盐
     */
    @Column(length = 16)
    private String salt;

    /**
     * 是否有效
     */
    @Column
    private boolean active = true;

    /**
     * 登录时间
     */
    @Transient
    private LocalDateTime loginTime = LocalDateTime.now();

    /**
     * 如果与session关联，session的id
     */
    @Transient
    private String sessionId;

    /**
     * 引用一个User
     */
    public static AbstractUser ref(Long id) {
        AbstractUser u = new VirtualUser();
        u.setId(id);
        return u;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (StringUtils.isEmpty(salt)) {
            this.salt = RandomStringUtils.randomAlphanumeric(16);
        }
        this.password = DigestUtils.sha256Hex(password + salt);

    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    private static final class VirtualUser extends AbstractUser {
        private static final long serialVersionUID = -7833194402212083265L;
    }
}
