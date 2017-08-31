package org.spin.data.core;

import org.spin.enhance.gson.annotation.PreventOverflow;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 基本实体类型
 * <p>定义了实体的部分通用字段，所有用户实体如无特殊需求，应从此类继承</p>
 * <p>Created by xuweinan on 2016/10/5.</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@MappedSuperclass
public abstract class AbstractEntity implements IEntity<Long>, Serializable {
    @Transient
    private static final long serialVersionUID = -6820468799272316789L;

    /**
     * 主键
     */
    @Id
    @PreventOverflow
    private Long id;

    /**
     * 记录创建者id
     */
    @Column
    @PreventOverflow
    private Long createUserId;

    /**
     * 创建时间，禁止更改
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 记录更新者id
     */
    @Column
    @PreventOverflow
    private Long updateUserId;

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
     * 排序号
     */
    @Column
    private float orderNo = 0;

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

    public Long getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(Long updateUserId) {
        this.updateUserId = updateUserId;
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

    public float getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(float orderNo) {
        this.orderNo = orderNo;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
