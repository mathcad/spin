package org.infrastructure.jpa.core;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.sql.Timestamp;

/**
 * 基本实体类型
 * <p>定义了实体的部分通用字段，所有用户实体如无特殊需求，应从此类继承</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@MappedSuperclass
public abstract class AbstractEntity implements IEntity<Long> {
    private static final long serialVersionUID = 4497191615275262107L;

    /**
     * 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * 冗余字段，记录创建者名称
     */
    @Column(updatable = false)
    private String createUserName;

    /**
     * 创建用户，禁止更改
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(updatable = false)
    private GenericUser createUser;

    /**
     * 创建时间，禁止更改
     */
    @Column(nullable = false, updatable = false)
    private Timestamp createTime;

    /**
     * 冗余字段，记录更新者名称
     */
    @Column
    private String lastUpdateUserName;

    /**
     * 最后更新用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private GenericUser lastUpdateUser;

    /**
     * 最后更新时间
     */
    @Column(nullable = false)
    private Timestamp lastUpdateTime;

    /**
     * 排序号
     */
    @Column(precision = 16, scale = 2)
    private double orderno = 1;

    /**
     * 是否有效用于逻辑删除
     */
    @Column
    private boolean valid = true;

    /**
     * 版本号用于并发控制
     */
    @Version
    private int version;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (this.getId() == null || obj == null || !(this.getClass().equals(obj.getClass()))) {
            return false;
        }
        IEntity that = (IEntity) obj;
        return this.getId().equals(that.getId());
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getCreateUserName() {
        return createUserName;
    }

    public void setCreateUserName(String createUserName) {
        this.createUserName = createUserName;
    }

    public GenericUser getCreateUser() {
        return createUser;
    }

    public void setCreateUser(GenericUser createUser) {
        this.createUser = createUser;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public String getLastUpdateUserName() {
        return lastUpdateUserName;
    }

    public void setLastUpdateUserName(String lastUpdateUserName) {
        this.lastUpdateUserName = lastUpdateUserName;
    }

    public GenericUser getLastUpdateUser() {
        return lastUpdateUser;
    }

    public void setLastUpdateUser(GenericUser lastUpdateUser) {
        this.lastUpdateUser = lastUpdateUser;
    }

    public Timestamp getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Timestamp lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public double getOrderno() {
        return orderno;
    }

    public void setOrderno(double orderno) {
        this.orderno = orderno;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
