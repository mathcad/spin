package org.spin.jpa.core;


import org.spin.sys.SessionUser;
import org.spin.util.DigestUtils;
import org.spin.util.RandomStringUtils;
import org.spin.util.StringUtils;

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
}
