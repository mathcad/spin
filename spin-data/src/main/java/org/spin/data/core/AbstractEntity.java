package org.spin.data.core;

import org.spin.data.util.EntityUtils;
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
 * 基础实体类型
 * <p>定义了实体的部分通用字段，所有用户实体如无特殊需求，应从此类继承</p>
 * <p>Created by xuweinan on 2016/10/5.</p>
 *
 * @author xuweinan
 * @version 1.2
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
     * 记录创建者用户名
     */
    @Column(length = 32)
    private String createUserName;

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
     * 记录更新者用户名
     */
    @Column(length = 32)
    private String updateUserName;

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
    public final <E extends AbstractEntity> E getDTO(final int depth) {
        //noinspection unchecked
        return (E) EntityUtils.getDTO(this, depth);
    }

    /**
     * 判断是否是同一实体。此方法只做同一性认定，不代表完全相同。
     * <p>同一性：指<b>相同类型</b>的实体具有相同的id，version。即标识与版本相同</p>
     *
     * @param o 待判断实体
     * @return 是否同一实体
     */
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
        return Objects.hash(id, version, getClass());
    }

    @Override
    public String toString() {
        return "AbstractEntity(" + getClass().getSimpleName() + "){" +
            "id=" + id +
            ", createUserId=" + createUserId +
            ", createUserName=" + createUserName +
            ", createTime=" + createTime +
            ", updateUserId=" + updateUserId +
            ", updateUserName=" + updateUserName +
            ", updateTime=" + updateTime +
            ", version=" + version +
            ", orderNo=" + orderNo +
            ", valid=" + valid +
            '}';
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

    public String getCreateUserName() {
        return createUserName;
    }

    public void setCreateUserName(String createUserName) {
        this.createUserName = createUserName;
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

    public String getUpdateUserName() {
        return updateUserName;
    }

    public void setUpdateUserName(String updateUserName) {
        this.updateUserName = updateUserName;
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
