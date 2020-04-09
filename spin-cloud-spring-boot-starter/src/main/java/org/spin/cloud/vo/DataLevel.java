package org.spin.cloud.vo;

import org.spin.core.trait.Evaluatable;

/**
 * 数据权限级别
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/8/28</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public enum DataLevel implements Evaluatable<Integer> {

    // region 部门控制维度
    /**
     * 全部
     */
    ALL_DEPT(1, "全部"),

    /**
     * 归属部门及下级部门
     */
    CURRENT_LOWER(3, "归属部门及下级部门"),

    /**
     * 本人及下级部门
     */
    LOWER(5, "本人及下级部门"),

    /**
     * 仅归属部门
     */
    CURRENT_DEPT(10, "仅归属部门"),

    /**
     * 仅本人
     */
    HIMSELF(15, "仅本人"),

    // endregion

    // region 岗位控制维度
    /**
     * 全部岗位
     */
    ALL_STATION(101, "全部"),

    /**
     * 同级岗位及下级岗位
     */
    COORDINATE_LOWER(103, "同级岗位及下级岗位"),

    /**
     * 同级岗位
     */
    COORDINATE(105, "同级岗位"),

    /**
     * 同岗位
     */
    CURRENT_STATION(110, "同岗位"),

    // endregion
    ;

    private final int value;
    private final String desc;

    DataLevel(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    public String getDescription() {
        return desc;
    }
}
