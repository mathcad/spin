package org.spin.mybatis.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;

/**
 * description 相关机构 系统数据权限
 *
 * @author wangy QQ 837195190
 * <p>Created by wangy on 2020/4/1.</p >
 */
public abstract class AbstractDataPermEntity<T extends AbstractDataPermEntity<T>> extends AbstractEntity<T> {

    /**
     * 部门ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long departmentId;

    /**
     * 岗位ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long stationId;

    /**
     * 自定义组织ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long customOrganId;

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getStationId() {
        return stationId;
    }

    public void setStationId(Long stationId) {
        this.stationId = stationId;
    }

    public Long getCustomOrganId() {
        return customOrganId;
    }

    public void setCustomOrganId(Long customOrganId) {
        this.customOrganId = customOrganId;
    }
}
