package org.spin.jpa.entity;


import org.spin.core.util.DigestUtils;
import org.spin.core.util.RandomStringUtils;
import org.spin.core.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * 用户的父类，定义了用户的通用属性
 * <p>Created by xuweinan on 2016/10/5.</p>
 *
 * @author xuweinan
 */
@MappedSuperclass
public abstract class AbstractUser<T extends AbstractUser<T>> extends AbstractEntity<T> {
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
    private Boolean active;

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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return super.toString() + " <=> AbstractUser(" + getClass().getSimpleName() + "){" +
            "userName='" + userName + '\'' +
            ", salt='" + salt + '\'' +
            ", active=" + active +
            "} ";
    }
}
