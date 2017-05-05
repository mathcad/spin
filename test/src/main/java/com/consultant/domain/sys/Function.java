package com.consultant.domain.sys;

import com.consultant.domain.enums.FunctionTypeE;
import org.hibernate.annotations.Type;
import org.spin.data.core.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * 功能模块
 * <p>Created by xuweinan on 2017/04/20.</p>
 *
 * @author xuweinan
 */
@Entity
@Table(name = "sys_function")
public class Function extends AbstractEntity {

    /**
     * 名称
     */
    @Column(length = 64)
    private String name;

    /**
     * 类型
     */
    @Type(type = "org.spin.data.extend.UserEnumType")
    private FunctionTypeE type;

    /**
     * 编码
     */
    @Column
    private String code;

    /**
     * url
     */
    @Column(length = 128)
    private String url;

    /**
     * 所需权限
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private Permission permission;

    /**
     * 上级功能
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private Function parent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FunctionTypeE getType() {
        return type;
    }

    public void setType(FunctionTypeE type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public Function getParent() {
        return parent;
    }

    public void setParent(Function parent) {
        this.parent = parent;
    }
}
