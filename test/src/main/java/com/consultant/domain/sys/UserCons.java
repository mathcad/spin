package com.consultant.domain.sys;

import org.spin.jpa.core.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * Created by wuqiang on 2017/4/24.
 */
@Entity
@Table(name = "bs_user_cons")
public class UserCons extends AbstractEntity {

    // 用户
    @ManyToOne
    private User user;

    // 顾问
    @ManyToOne
    private Cons cons;

    // 用户顾问聘用类型
    @ManyToOne(fetch = FetchType.LAZY)
    Dict type;

    //有效次数
    @Column
    private Integer vaildTimes;

    // 有效截止时间
    @Column
    private LocalDateTime vaildDate;

    public Cons getCons() {
        return cons;
    }

    public void setCons(Cons cons) {
        this.cons = cons;
    }

    public Dict getType() {
        return type;
    }

    public void setType(Dict type) {
        this.type = type;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getVaildDate() {
        return vaildDate;
    }

    public void setVaildDate(LocalDateTime vaildDate) {
        this.vaildDate = vaildDate;
    }

    public Integer getVaildTimes() {
        return vaildTimes;
    }

    public void setVaildTimes(Integer vaildTimes) {
        this.vaildTimes = vaildTimes;
    }
}
