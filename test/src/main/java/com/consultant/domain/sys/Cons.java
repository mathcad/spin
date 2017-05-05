package com.consultant.domain.sys;

/**
 * Created by wuqiang on 2017/4/21.
 */

import org.spin.data.core.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * 顾问
 */
@Entity
@Table(name = "bs_cons")
public class Cons extends AbstractEntity{

    // 是否首页展示
    @Column
    private boolean isHome;


    // 顾问展示图片
    @ManyToOne
    private File showImg;

    // 标签 以逗号分隔‘，’
    @Column
    private String label;

    // 顾问简介
    @Column
    private String intro;

    // 关联用户
    @ManyToOne
    private User user;

    // 顾问详细介绍
    @Column
    private String detail;

    @Column
    private Integer popVal;

    public Integer getPopVal() {
        return popVal;
    }

    public void setPopVal(Integer popVal) {
        this.popVal = popVal;
    }


    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public boolean isHome() {
        return isHome;
    }

    public void setHome(boolean home) {
        isHome = home;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public File getShowImg() {
        return showImg;
    }

    public void setShowImg(File showImg) {
        this.showImg = showImg;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
