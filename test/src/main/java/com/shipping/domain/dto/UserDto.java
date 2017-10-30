package com.shipping.domain.dto;

import com.shipping.domain.enums.UserTypeE;
import com.shipping.domain.sys.Organization;
import com.shipping.domain.sys.User;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * <p>Created by xuweinan on 2017/9/13.</p>
 *
 * @author xuweinan
 */
public class UserDto implements Serializable {
    private static final long serialVersionUID = -4926348221616077329L;

    private String id;

    private String userName;

    private String nickname;

    private String realName;

    private String headImg;

    private String email;

    private String mobile;

    private UserTypeE userType;

    private String about;

    private Organization organ;

    private LocalDateTime createTime;

    public UserDto(User user) {
        this.id = user.getId().toString();
        this.userName = user.getUserName();
        this.nickname = user.getNickname();
        this.realName = user.getRealName();
        this.headImg = Objects.isNull(user.getHeadImg()) ? null : user.getHeadImg().getFilePath();
        this.email = user.getEmail();
        this.mobile = user.getMobile();
        this.userType = user.getUserType();
        this.about = user.getAbout();
        this.organ = user.getOrgan();
        this.createTime = user.getCreateTime();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
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

    public UserTypeE getUserType() {
        return userType;
    }

    public void setUserType(UserTypeE userType) {
        this.userType = userType;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public Organization getOrgan() {
        return organ;
    }

    public void setOrgan(Organization organ) {
        this.organ = organ;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
