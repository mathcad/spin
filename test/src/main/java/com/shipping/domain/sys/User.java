package com.shipping.domain.sys;

import com.shipping.domain.enums.UserTypeE;
import org.hibernate.annotations.Type;
import org.spin.data.core.AbstractUser;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统用户信息
 */
@Entity
@Table(name = "sys_user", indexes = {@Index(columnList = "mobile"), @Index(columnList = "openId")})
public class User extends AbstractUser {
    private static final long serialVersionUID = -7875157725232505055L;

    @Column(length = 64)
    private String nickname;

    @Column(length = 32)
    private String realName;

    @ManyToOne
    private File headImg;

    @Column(length = 64)
    private String email;

    @Column(length = 14, unique = true)
    private String mobile;

    @Column(length = 64)
    private String openId;

    @Type(type = "org.spin.data.extend.UserEnumType")
    private UserTypeE userType;

    @Column(length = 128)
    private String about;

    /**
     * 登记机构
     */
    @ManyToOne
    private Organization organ;

    /**
     * 所属机构
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "sys_user_organ", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "organ_id"))
    private List<Organization> organs = new ArrayList<>();

    /**
     * 拥有角色
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "sys_user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles = new ArrayList<>();

    /**
     * 拥有权限
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "sys_user_permission", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private List<Permission> permissions = new ArrayList<>();

    public List<Organization> getOrgans() {
        return organs;
    }

    public void setOrgans(List<Organization> organs) {
        this.organs = organs;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public File getHeadImg() {
        return headImg;
    }

    public void setHeadImg(File headImg) {
        this.headImg = headImg;
    }

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

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public UserTypeE getUserType() {
        return userType;
    }

    public void setUserType(UserTypeE userType) {
        this.userType = userType;
    }

    public Organization getOrgan() {
        return organ;
    }

    public void setOrgan(Organization organ) {
        this.organ = organ;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }
}
