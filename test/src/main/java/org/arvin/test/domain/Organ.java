package org.arvin.test.domain;

import org.hibernate.annotations.Type;
import org.infrastructure.jpa.core.AbstractEntity;
import org.infrastructure.jpa.core.IEntity;

import javax.persistence.*;


/**
 * 机构表实体类
 *
 * @author lijian
 * @version V1.0
 * @contact 电话: 18055335518, QQ: 2630418388
 * @create 2015-4-21 21:28:24
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "sys_organ")
public class Organ implements java.io.Serializable, IEntity<Long> {

    /**
     * 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    /**
     * 编码
     */
    @Column(length = 10)
    String code;

    /**
     * 机构名称
     */
    @Column(length = 50)
    String name;

    /**
     * 机构别名（简称）
     */
    @Column(length = 50)
    String alias;

    /**
     * 上级机构
     */
    @ManyToOne(fetch = FetchType.LAZY)
    Organ parent;

    /**
     * 继承的路径编码，id，分割组合
     */
    @Column(length = 200)
    String idPath;

    /**
     * 地址
     */
    @Column(length = 100)
    String address;

    /**
     * 联系电话
     */
    @Column(length = 20)
    String phone;

    /**
     * 备注
     */
    @Column(length = 100)
    String remark;

    /**
     * 团队类型
     */
    @Type(type = "org.infrastructure.jpa.extend.UserEnumType")
    OrganTeamTypeE teamType;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Organ getParent() {
        return parent;
    }

    public void setParent(Organ parent) {
        this.parent = parent;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getIdPath() {
        return idPath;
    }

    public void setIdPath(String idPath) {
        this.idPath = idPath;
    }

    public OrganTeamTypeE getTeamType() {
        return teamType;
    }

    public void setTeamType(OrganTeamTypeE teamType) {
        this.teamType = teamType;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }
}
