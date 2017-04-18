package org.spin.jpa.core;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 基本实体类型
 * <p>定义了实体的部分通用字段，所有用户实体如无特殊需求，应从此类继承</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@MappedSuperclass
public abstract class AbstractEntity implements IEntity<Long>, Serializable {

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
    private BaseUser createBy;

    /**
     * 创建时间，禁止更改
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 记录更新者
     */
    @ManyToOne
    private BaseUser updateBy;

    /**
     * 最后更新时间
     */
    @Column(nullable = false)
    private LocalDateTime updateTime;

    /**
     * 版本号用于并发控制
     */
    @Version
    private int version;

    /**
     * 标记逻辑删除
     */
    @Column
    private boolean valid = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractEntity that = (AbstractEntity) o;
        return version == that.version &&
            Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version, this.getClass());
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public BaseUser getCreateBy() {
        return createBy;
    }

    public void setCreateBy(BaseUser createBy) {
        this.createBy = createBy;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public BaseUser getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(BaseUser updateBy) {
        this.updateBy = updateBy;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
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
