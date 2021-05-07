package org.spin.cloud.vo;

import java.util.Collections;
import java.util.Set;

/**
 * 员工信息
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/4/7</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class SessionEmpInfo {
    private final long userId;
    private final long empId;
    private final long enterpriseId;

    private final Set<Long> depts;
    private final Set<Long> stations;
    private final Set<Long> organs;

    public SessionEmpInfo(long userId, long empId, long enterpriseId, Set<Long> depts, Set<Long> stations, Set<Long> organs) {
        this.userId = userId;
        this.empId = empId;
        this.enterpriseId = enterpriseId;
        this.depts = depts;
        this.stations = stations;
        this.organs = organs;
    }

    private SessionEmpInfo(long userId) {
        this.userId = userId;
        empId = 0L;
        enterpriseId = 0L;

        depts = Collections.emptySet();
        stations = Collections.emptySet();
        organs = Collections.emptySet();
    }

    /**
     * 构造一个非企业用户信息
     *
     * @param userId 用户ID
     * @return 非企业用户信息
     */
    public static SessionEmpInfo newNonEntUser(long userId) {
        return new SessionEmpInfo(userId);
    }

    public long getUserId() {
        return userId;
    }

    public long getEmpId() {
        return empId;
    }

    public long getEnterpriseId() {
        return enterpriseId;
    }

    public Set<Long> getDepts() {
        return depts;
    }

    public Set<Long> getStations() {
        return stations;
    }

    public Set<Long> getOrgans() {
        return organs;
    }
}
