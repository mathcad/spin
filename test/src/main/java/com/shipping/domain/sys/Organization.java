package com.shipping.domain.sys;

import com.shipping.domain.enums.OrganizationTypeE;
import org.hibernate.annotations.Type;
import org.spin.data.core.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * 组织机构
 * <p>Created by xuweinan on 2017/07/20.</p>
 *
 * @author xuweinan
 */
@Entity
@Table(name = "sys_organ")
public class Organization extends AbstractEntity {
    private static final long serialVersionUID = 8610089447094514827L;

    /**
     * 名称
     */
    @Column(length = 64, unique = true)
    private String name;

    /**
     * 编码
     */
    @Column(length = 64, unique = true)
    private String code;

    /**
     * 简称
     */
    @Column(length = 16)
    private String alias;

    /**
     * 联系方式
     */
    @Column(length = 32)
    private String tel;

    /**
     * 地址
     */
    @Column(length = 128)
    private String address;

    /**
     * 类型
     */
    @Type(type = "org.spin.data.extend.UserEnumType")
    private OrganizationTypeE type;

    /**
     * 上级机构
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private Organization parent;

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

    @Column
    private String remark;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OrganizationTypeE getType() {
        return type;
    }

    public void setType(OrganizationTypeE type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Organization getParent() {
        return parent;
    }

    public void setParent(Organization parent) {
        this.parent = parent;
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
