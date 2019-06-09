package org.spin.common.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import org.spin.core.gson.annotation.PreventOverflow;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 抽象实体
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/18</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public abstract class AbstractEntity implements Serializable {

    /**
     * 主键
     */
    @TableId
    @PreventOverflow
    private Long id;

    /**
     * 逻辑删除标识
     */
    @TableLogic
    @TableField
    private Integer valid = 1;

    /**
     * 备注
     */
    @TableField
    private String remark;

    /**
     * 创建人ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy = 0L;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    @PreventOverflow
    private String createUsername = "";

    /**
     * 创建时间
     */
    @TableField
    private LocalDateTime createTime;

    /**
     * 更新人ID
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @PreventOverflow
    private Long updateBy = 0L;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateUsername = "";

    /**
     * 更新时间
     */
    @TableField
    private LocalDateTime updateTime;

    /**
     * 版本号用于并发控制
     */
    @Version
    private Integer version = 0;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getValid() {
        return valid;
    }

    public void setValid(Integer valid) {
        this.valid = valid;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
