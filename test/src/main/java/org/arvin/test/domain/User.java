/*
 *  Copyright 2002-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.arvin.test.domain;

import org.hibernate.annotations.Type;
import org.infrastructure.jpa.core.AbstractEntity;
import org.infrastructure.shiro.SessionUser;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 系统用户信息
 *
 * @author zhou
 * @contact 电话: 18963752887, QQ: 251915460
 * @create 2015年3月18日 上午10:48:13
 * @version V1.0
 */
@Entity
@Table(name = "sys_user")
public class User extends AbstractEntity implements SessionUser, java.io.Serializable {

    /** 登录名 */
    @Column(length = 50)
    String name;

    /** 真实姓名 */
    @Column(length = 50)
    String realName;

    /** 加密密码 */
    @Column(length = 250)
    String password;

    /** 手机号码（需验证） */
    @Column(length = 20)
    String mobile;

    /** 邮箱（需验证） */
    String email;

    /** 邮箱验证 */
    boolean emailValid;

    /** 内部用户(0), 货主(1), 司机(2); */
    @Column(length = 2)
    @Type(type = "com.gsh56.infrastructure.jpa.core.UserEnumType")
    UserTypeE type;

    /** 人事机构 */
    @ManyToOne(fetch = FetchType.LAZY)
    public Organ organ;

    /** 拥有角色 */
    @JoinTable(name = "sys_user_role", inverseJoinColumns = @JoinColumn(name = "role"), joinColumns = @JoinColumn(name = "user"))
    @ManyToMany(cascade = CascadeType.REFRESH, targetEntity = Role.class, fetch = FetchType.LAZY)
    List<Role> roles = new ArrayList<Role>();

    /** 分管机构 */
    @JoinTable(name = "sys_user_organ", inverseJoinColumns = @JoinColumn(name = "organ"), joinColumns = @JoinColumn(name = "user"))
    @ManyToMany(cascade = CascadeType.REFRESH, targetEntity = Organ.class, fetch = FetchType.LAZY)
    List<Organ> organs = new ArrayList<Organ>();

    /** 注册时间 */
    @Temporal(TemporalType.TIMESTAMP)
    Date registerTime;

    /** 最后登录时间 */
    @Temporal(TemporalType.TIMESTAMP)
    Date lastLoginTime;

    boolean firstLogin = false;

    /**用于单点登录的access_token*/
    @Column(length = 50)
    String accessToken;

    /** 单点登录access_token令牌有效期 */
    @Temporal(TemporalType.TIMESTAMP)
    Date accessTokenExpired;

    public User() {
    }

    public User(String name, String password, String mobile, UserTypeE userType, short internetProductId, Date registerTime) {
        this.name = name;
        this.password = password;
        this.mobile = mobile;
        this.type = userType;
        this.registerTime = registerTime;
    }


//	String salt;

    /*
     * (non-Javadoc)
     *
     * @see com.gsh56.infrastructure.shiro.AUser#getLoginName()
     */
    @Override
    public String getLoginName() {
        return this.name;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.gsh56.infrastructure.shiro.AUser#setSessionId(java.lang.String)
     */
    @Override
    public void setSessionId(String sessionId) {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailValid() {
        return emailValid;
    }

    public void setEmailValid(boolean emailValid) {
        this.emailValid = emailValid;
    }

    public UserTypeE getType() {
        return type;
    }

    public void setType(UserTypeE type) {
        this.type = type;
    }

    public Organ getOrgan() {
        return organ;
    }

    public void setOrgan(Organ organ) {
        this.organ = organ;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public List<Organ> getOrgans() {
        return organs;
    }

    public void setOrgans(List<Organ> organs) {
        this.organs = organs;
    }

    public Date getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(Date registerTime) {
        this.registerTime = registerTime;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public boolean isFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(boolean firstLogin) {
        this.firstLogin = firstLogin;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Date getAccessTokenExpired() {
        return accessTokenExpired;
    }

    public void setAccessTokenExpired(Date accessTokenExpired) {
        this.accessTokenExpired = accessTokenExpired;
    }

}

