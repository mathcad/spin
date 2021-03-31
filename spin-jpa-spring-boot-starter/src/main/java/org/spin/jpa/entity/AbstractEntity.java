package org.spin.jpa.entity;

import org.spin.core.gson.annotation.PreventOverflow;
import org.spin.jpa.util.EntityUtils;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类型
 * <p>定义了实体的部分通用字段，所有用户实体如无特殊需求，应从此类继承</p>
 * <p>Created by xuweinan on 2016/10/5.</p>
 *
 * @author xuweinan
 * @version 1.2
 */
@MappedSuperclass
public abstract class AbstractEntity<T extends AbstractEntity<T>> extends BasicEntity<T> implements Serializable {
    private static final long serialVersionUID = -6820468799272316789L;

    /**
     * 记录创建者id
     */
    @Column
    @PreventOverflow
    private Long createBy;

    /**
     * 记录创建者用户名
     */
    @Column(length = 32)
    private String createUsername;

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
    private Long updateBy;

    /**
     * 记录更新者用户名
     */
    @Column(length = 32)
    private String updateUsername;

    /**
     * 最后更新时间
     */
    @Column(nullable = false)
    private LocalDateTime updateTime;

    /**
     * 标记逻辑删除
     */
    @Column
    private Boolean valid;

    /**
     * 备注
     */
    @Column(length = 120)
    private String remark;

    /**
     * 扩展字段1
     */
    @Transient
    private Serializable extInfo1;

    /**
     * 扩展字段2
     */
    @Transient
    private Serializable extInfo2;

    /**
     * 扩展字段3
     */
    @Transient
    private Serializable extInfo3;

    /**
     * 获取当前实体的DTO。DTO是当前实体的浅拷贝。
     *
     * @param depth 属性解析深度
     * @param <E>   实体类型
     * @return DTO
     */
    @SuppressWarnings("unchecked")
    public final <E extends AbstractEntity<E>> E getDTO(final int depth) {
        return (E) EntityUtils.getDTO(this, depth);
    }

    @Override
    public String toString() {
        return super.toString() + " <=> AbstractEntity(" + getClass().getSimpleName() + "){" +
            ", createBy=" + createBy +
            ", createUsername=" + createUsername +
            ", createTime=" + createTime +
            ", updateBy=" + updateBy +
            ", updateUsername=" + updateUsername +
            ", updateTime=" + updateTime +
            ", valid=" + valid +
            '}';
    }

    public Long getCreateBy() {
        return createBy;
    }

    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    public String getCreateUsername() {
        return createUsername;
    }

    public void setCreateUsername(String createUsername) {
        this.createUsername = createUsername;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }

    public String getUpdateUsername() {
        return updateUsername;
    }

    public void setUpdateUsername(String updateUsername) {
        this.updateUsername = updateUsername;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public Boolean getValid() {
        return valid;
    }

    @Override
    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Serializable getExtInfo1() {
        return extInfo1;
    }

    public void setExtInfo1(Serializable extInfo1) {
        this.extInfo1 = extInfo1;
    }

    public Serializable getExtInfo2() {
        return extInfo2;
    }

    public void setExtInfo2(Serializable extInfo2) {
        this.extInfo2 = extInfo2;
    }

    public Serializable getExtInfo3() {
        return extInfo3;
    }

    public void setExtInfo3(Serializable extInfo3) {
        this.extInfo3 = extInfo3;
    }
}
