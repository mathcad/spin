package org.spin.mybatis.vo;

import io.swagger.annotations.ApiModelProperty;
import org.spin.core.gson.annotation.PreventOverflow;
import org.spin.mybatis.entity.AbstractEntity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 抽象视图Vo
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/12/9</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public abstract class AbstractViewVo<T, E extends AbstractEntity> implements VoEntityMapper<T, E>, Serializable {

    @ApiModelProperty(value = "ID", example = "1")
    @PreventOverflow
    private Long id;

    /**
     * 数据版本
     */
    @ApiModelProperty(value = "数据版本", example = "0")
    private Integer version = 0;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人", example = "张三")
    private String createUsername = "";

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", example = "2019-12-01 00:00:00")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @ApiModelProperty(value = "更新人", example = "李四")
    private String updateUsername = "";

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间", example = "2019-12-01 12:00:00")
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注", example = "这是备注信息")
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
