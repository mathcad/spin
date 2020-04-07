package org.spin.cloud.vo;

/**
 * 数据权限级别
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/8/28</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public enum DataLevel {

    // region 部门控制维度
    /**
     * 全部
     */
    ALL_DEPT(1, "全部"),

    /**
     * 仅归属部门
     */
    CURRENT_DEPT(3, "仅归属部门"),

    /**
     * 本人及下级部门
     */
    LOWER(5, "本人及下级部门"),

    /**
     * 归属部门及下级部门
     */
    CURRENT_LOWER(10, "归属部门及下级部门"),

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
     * 同岗位
     */
    CURRENT_STATION(103, "同岗位"),

    /**
     * 同级岗位
     */
    COORDINATE(105, "同级岗位"),

    /**
     * 同级岗位及下级岗位
     */
    COORDINATE_LOWER(110, "同级岗位及下级岗位"),

    // endregion
    ;

    private final int value;
    private final String desc;

    DataLevel(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public Integer getValue() {
        return value;
    }

    public String getDescription() {
        return desc;
    }
}
