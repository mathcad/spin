package org.infrastructure.jpa.core;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import java.util.Date;
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

    /**
     * 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 记录创建者名称
     */
    @ManyToOne
    private AbstractUser createBy;

    /**
     * 创建时间，禁止更改
     */
    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    /**
     * 记录更新者
     */
    @ManyToOne
    private AbstractUser updateBy;

    /**
     * 最后更新时间
     */
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    /**
     * 版本号用于并发控制
     */
    @Version
    private int version;

    @Column
    private boolean valid = true;

    @Override
    public boolean equals(Object obj) {
        return this == obj || this.getId() != null && obj != null && this.getClass().equals(obj.getClass()) && this.getId().equals(((IEntity) obj).getId());
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

    public AbstractUser getCreateBy() {
        return createBy;
    }

    public void setCreateBy(AbstractUser createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public AbstractUser getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(AbstractUser updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}