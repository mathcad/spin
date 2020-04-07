package org.spin.cloud.vo;

import java.util.Set;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/4/7</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class DataPermInfo {

    /**
     * 是否限制数据权限
     */
    private boolean hasDataLimit = false;

    /**
     * 部门维度的数据权限
     */
    private Set<Long> deptIds;

    /**
     * 岗位维度的数据权限
     */
    private Set<Long> stationIds;

    /**
     * 是否仅自己
     */
    private Boolean himself;

    public Set<Long> getDeptIds() {
        return deptIds;
    }

    public void setDeptIds(Set<Long> deptIds) {
        this.deptIds = deptIds;
    }

    public Set<Long> getStationIds() {
        return stationIds;
    }

    public void setStationIds(Set<Long> stationIds) {
        this.stationIds = stationIds;
    }

    public Boolean getHimself() {
        return himself;
    }

    public void setHimself(Boolean himself) {
        this.himself = himself;
    }
}
