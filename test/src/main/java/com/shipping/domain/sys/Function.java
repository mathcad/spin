package com.shipping.domain.sys;

import com.shipping.domain.enums.FunctionTypeE;
import org.hibernate.annotations.Type;
import org.spin.data.core.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * 功能项
 * <p>Created by xuweinan on 2017/07/20.</p>
 *
 * @author xuweinan
 */
@Entity
@Table(name = "sys_function")
public class Function extends AbstractEntity {

    /**
     * 名称
     */
    @Column(length = 64, unique = true)
    private String name;

    /**
     * 类型
     */
    @Type(type = "org.spin.data.extend.UserEnumType")
    private FunctionTypeE type;

    /**
     * 编码
     */
    @Column(length = 64, unique = true)
    private String code;

    /**
     * 图标
     */
    @Column(length = 64)
    private String icon;

    /**
     * 路径
     */
    @Column(length = 128)
    private String link;

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

    /**
     * 功能组
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private FunctionGroup group;

    /**
     * id线索
     */
    @Column
    private String idPath;

    /**
     * 是否叶子节点(用于加速查找)
     */
    @Column
    private boolean isLeaf = true;

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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
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

    public FunctionGroup getGroup() {
        return group;
    }

    public void setGroup(FunctionGroup group) {
        this.group = group;
    }

    public String getIdPath() {
        return idPath;
    }

    public void setIdPath(String idPath) {
        this.idPath = idPath;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean leaf) {
        isLeaf = leaf;
    }
}
