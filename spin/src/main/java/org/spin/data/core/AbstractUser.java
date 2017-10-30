package org.spin.data.core;


import org.spin.core.session.SessionUser;
import org.spin.core.util.DigestUtils;
import org.spin.core.util.RandomStringUtils;
import org.spin.core.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.io.Serializable;
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
    private Serializable sessionId;

    /**
     * 引用一个User
     */
    public static AbstractUser ref(Long id) {
        AbstractUser u = new VirtualUser();
        u.setId(id);
        return u;
    }

    /**
     * 用户名
     */
    public String getUserName() {
        return userName;
    }

    /**
     * 用户名
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 密码
     */
    public void setPassword(String password) {
        if (StringUtils.isEmpty(salt)) {
            this.salt = RandomStringUtils.randomAlphanumeric(16);
        }
        this.password = DigestUtils.sha256Hex(password + salt);

    }

    /**
     * 盐
     */
    public String getSalt() {
        return salt;
    }

    /**
     * 盐
     */
    public void setSalt(String salt) {
        this.salt = salt;
    }

    /**
     * 是否有效
     */
    @Override
    public boolean isActive() {
        return active;
    }

    /**
     * 是否有效
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * 登录时间
     */
    @Override
    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    /**
     * 登录时间
     */
    @Override
    public Serializable getSessionId() {
        return sessionId;
    }

    /**
     * 如果与session关联，session的id
     */
    @Override
    public void setSessionId(Serializable sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        return "AbstractUser{" +
            "userName='" + userName + '\'' +
            ", password='" + password + '\'' +
            ", salt='" + salt + '\'' +
            ", active=" + active +
            ", loginTime=" + loginTime +
            ", sessionId='" + sessionId + '\'' +
            "} " + super.toString();
    }

    private static final class VirtualUser extends AbstractUser {
        private static final long serialVersionUID = -7833194402212083265L;
    }
}
