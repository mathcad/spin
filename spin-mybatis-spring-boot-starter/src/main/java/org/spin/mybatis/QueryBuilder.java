package org.spin.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.spin.cloud.vo.CurrentUser;
import org.spin.cloud.vo.SessionEmpInfo;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.StringUtils;
import org.spin.mybatis.entity.AbstractDataPermEntity;

import java.util.Collections;

/**
 * description query builder
 *
 * @author wangy QQ 837195190
 * <p>Created by wangy on 2019/3/13.</p>
 */
public interface QueryBuilder {

    static <T> QueryWrapper<T> query() {
        return new QueryWrapper<>();
    }

    static <T> LambdaQueryWrapper<T> lambdaQuery() {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        return queryWrapper.lambda();
    }

    static <T> QueryWrapper<T> emptyQuery() {
        return Wrappers.emptyWrapper();
    }

    static <T extends AbstractDataPermEntity> LambdaQueryWrapper<T> withDataPermission(LambdaQueryWrapper<T> queryWrapper) {
        return queryWrapper.in(AbstractDataPermEntity::getDepartmentId, Collections.emptySet());
    }

    static String buildDataPermSql(String prefix, String alias) {
        prefix = StringUtils.trimToEmpty(prefix);
        alias = StringUtils.trimToEmpty(alias);
        // 判断按哪个维度控制(部门或岗位)

        SessionEmpInfo empInfo = CurrentUser.getEnterpriseInfo();

        Boolean himself = null;
        if (Boolean.TRUE.equals(himself)) {
            return prefix + " (" + alias + ".create_by = " + empInfo.getUserId() + ")";
        } else {
            // 没有任何权限
            if (CollectionUtils.isEmpty(empInfo.getDepts()) && CollectionUtils.isEmpty(empInfo.getStations())) {
                return prefix + " (1 = 2)";
            }

            StringBuilder sql = new StringBuilder();
//            sql.append(prefix).append(" ").append("(");
//            if (null == himself) {
//                sql.append(alias).append(".").append("create_by = ").append(empInfo.getUserId()).append(" OR ");
//            }
//
//            if (CollectionUtils.isNotEmpty()) {
//                sql.append(alias).append(".").append("department_id IN (").append(StringUtils.join(deptIds, ",")).append(") OR ");
//            }
//
//            if (CollectionUtils.isNotEmpty(stationIds)) {
//                sql.append(alias).append(".").append("station_id IN (").append(StringUtils.join(stationIds, ",")).append(") OR ");
//            }

            return sql.substring(0, sql.length() - 4);
        }
    }
}
