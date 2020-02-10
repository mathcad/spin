package org.spin.common.db.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import org.spin.core.gson.annotation.PreventOverflow;

import java.time.LocalDateTime;

/**
 * 抽象实体
 * <p>继承自基础实体, 增加了创建人, 更新人, 逻辑删除与备注信息</p>
 * <p>Created by xuweinan on 2019/3/18</p>
 *
 * @author xuweinan
 * @version 1.1
 * @see org.spin.common.db.entity.BasicEntity BasicEntity
 */
public abstract class AbstractEntity extends BasicEntity {

    /**
     * 创建人ID
     */
    @TableField(fill = FieldFill.INSERT)
    @PreventOverflow
    private Long createBy = 0L;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
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
     * 逻辑删除标识
     */
    @TableLogic(value = "1", delval = "0")
    @TableField
    private Byte valid = 1;

    /**
     * 备注
     */
    @TableField
    private String remark;

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

    public Byte getValid() {
        return valid;
    }

    public void setValid(Byte valid) {
        this.valid = valid;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
