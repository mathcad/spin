package org.infrastructure.jpa.core;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.sql.Timestamp;
import java.util.Random;

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
     * 记录创建者
     */
    @ManyToOne
    private AbstractUser createUser;

    /**
     * 创建时间，禁止更改
     */
    @Column(nullable = false, updatable = false)
    private Timestamp createTime;

    /**
     * 记录更新者名称
     */
    @Column
    private AbstractUser updateUser;

    /**
     * 最后更新时间
     */
    @Column(nullable = false)
    private Timestamp updateTime;

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
        return this == obj || this.id != null && obj != null && this.getClass().equals(obj.getClass()) && this.id.equals(((IEntity) obj).getId());
    }

    @Override
    public int hashCode() {
        Long i = this.id == null ? new Random().nextLong() : this.id;
        String identifier = this.getClass().getName() + i.toString();
        return identifier.hashCode();
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public AbstractUser getCreateUser() {
        return createUser;
    }

    public void setCreateUser(AbstractUser createUser) {
        this.createUser = createUser;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public AbstractUser getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(AbstractUser updateUser) {
        this.updateUser = updateUser;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
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
