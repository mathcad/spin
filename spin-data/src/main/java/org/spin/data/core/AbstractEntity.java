package org.spin.data.core;

import org.spin.core.gson.annotation.PreventOverflow;
import org.spin.data.util.EntityUtils;

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
    private Long createBy = 0L;

    /**
     * 记录创建者用户名
     */
    @Column(length = 32)
    private String createUsername = "";

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
    private Long updateBy = 0L;

    /**
     * 记录更新者用户名
     */
    @Column(length = 32)
    private String updateUsername = "";

    /**
     * 最后更新时间
     */
    @Column(nullable = false)
    private LocalDateTime updateTime;

    /**
     * 版本号用于并发控制
     */
    @Version
    private Integer version = 0;

    /**
     * 标记逻辑删除
     */
    @Column
    private Boolean valid = true;

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
        return Objects.equals(version, that.version) &&
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
            ", createBy=" + createBy +
            ", createUsername=" + createUsername +
            ", createTime=" + createTime +
            ", updateBy=" + updateBy +
            ", updateUsername=" + updateUsername +
            ", updateTime=" + updateTime +
            ", version=" + version +
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
    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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
