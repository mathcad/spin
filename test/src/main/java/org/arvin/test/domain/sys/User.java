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

package org.arvin.test.domain.sys;

import org.arvin.test.domain.enums.UserTypeE;
import org.hibernate.annotations.Type;
import org.spin.jpa.core.AbstractUser;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统用户信息
 */
@Entity
@Table(name = "sys_user")
public class User extends AbstractUser {

    @Column(length = 64)
    private String email;

    @Column(length = 14)
    private String mobile;

    @Type(type = "org.spin.jpa.extend.UserEnumType")
    private UserTypeE userType;

    @ManyToMany(fetch = FetchType.LAZY)
    private List<SysRole> roleList = new ArrayList<>();

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public UserTypeE getUserType() {
        return userType;
    }

    public void setUserType(UserTypeE userType) {
        this.userType = userType;
    }

    public List<SysRole> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<SysRole> roleList) {
        this.roleList = roleList;
    }
}
