package com.shipping.domain.sys;

import org.spin.data.core.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * <p>Created by xuweinan on 2017/4/20.</p>
 *
 * @author xuweinan
 */
@Entity
@Table(name = "sys_permission")
public class Permission extends AbstractEntity {
    private static final long serialVersionUID = -724121483134748879L;

    @Column(length = 64, unique = true)
    private String name;

    @Column(length = 64, unique = true)
    private String code;

    @Column
    private String remark;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}