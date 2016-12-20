package org.infrastructure.jpa.core;


import org.infrastructure.sys.SessionUser;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;

/**
 * Created by xuweinan on 2016/10/5.
 *
 * @author xuweinan
 */
@Entity
@Table(name = "sys_user")
public class AbstractUser extends AbstractEntity implements SessionUser {

    @Column(length = 64)
    private String userName;

    @Transient
    private Date loginTime = new Date();

    @Transient
    private String sessionId;

    /**
     * 引用一个User
     */
    public static AbstractUser ref(Long id) {
        AbstractUser u = new AbstractUser();
        u.setId(id);
        return u;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public Date getLoginTime() {
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