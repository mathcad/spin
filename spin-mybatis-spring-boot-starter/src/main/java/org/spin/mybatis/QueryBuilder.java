package org.spin.mybatis;

import com.baomidou.mybatisplus.core.conditions.AbstractLambdaWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.spin.cloud.vo.CurrentUser;
import org.spin.cloud.vo.DataPermInfo;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.StringUtils;
import org.spin.mybatis.entity.AbstractDataPermEntity;
import org.spin.mybatis.entity.AbstractEntity;

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

    static <T extends AbstractDataPermEntity<T>> LambdaQueryWrapper<T> lambdaDataPermQuery() {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        return withDataPermission(queryWrapper.lambda());
    }

    static <T extends AbstractDataPermEntity<T>> QueryWrapper<T> dataPermQuery() {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        return withDataPermission(queryWrapper);
    }

    static <T extends AbstractDataPermEntity<T>> QueryWrapper<T> withDataPermission(QueryWrapper<T> queryWrapper) {
        DataPermInfo dataPermInfo = CurrentUser.getCurrentNonNull().getDataPermInfo();
        if (null == dataPermInfo) {
            return queryWrapper;
        }

        if (Boolean.TRUE.equals(dataPermInfo.getHimself())) {
            queryWrapper.and(i -> i.eq("create_by", CurrentUser.getCurrent().getId()));
        } else {
            queryWrapper.and(i -> i.or().eq(null == dataPermInfo.getHimself(), "create_by", CurrentUser.getCurrent().getId())
                .or().in(CollectionUtils.isNotEmpty(dataPermInfo.getDeptIds()), "department_id", dataPermInfo.getDeptIds())
                .or().in(CollectionUtils.isNotEmpty(dataPermInfo.getStationIds()), "station_id", dataPermInfo.getStationIds())
            );
        }

        return queryWrapper;
    }

    static <E extends AbstractDataPermEntity<E>, T extends AbstractLambdaWrapper<E, T>> T withDataPermission(T queryWrapper) {
        DataPermInfo dataPermInfo = CurrentUser.getCurrentNonNull().getDataPermInfo();
        if (null == dataPermInfo) {
            return queryWrapper;
        }

        if (Boolean.TRUE.equals(dataPermInfo.getHimself())) {
            queryWrapper.and(i -> i.eq(AbstractEntity::getCreateBy, CurrentUser.getCurrent().getId()));
        } else {
            queryWrapper.and(i -> i.or().eq(null == dataPermInfo.getHimself(), AbstractEntity::getCreateBy, CurrentUser.getCurrent().getId())
                .or().in(CollectionUtils.isNotEmpty(dataPermInfo.getDeptIds()), AbstractDataPermEntity::getDepartmentId, dataPermInfo.getDeptIds())
                .or().in(CollectionUtils.isNotEmpty(dataPermInfo.getStationIds()), AbstractDataPermEntity::getStationId, dataPermInfo.getStationIds())
            );
        }

        return queryWrapper;
    }

    static String buildDataPermSql(String prefix, String alias) {
        prefix = StringUtils.trimToEmpty(prefix);
        alias = StringUtils.trimToEmpty(alias);
        DataPermInfo dataPermInfo = CurrentUser.getCurrentNonNull().getDataPermInfo();

        if (!dataPermInfo.isHasDataLimit()) {
            return "";
        }

        if (Boolean.TRUE.equals(dataPermInfo.getHimself())) {
            return prefix + " (" + alias + ".create_by = " + CurrentUser.getCurrent().getId() + ")";
        } else {
            // 没有任何权限
            if (CollectionUtils.isEmpty(dataPermInfo.getDeptIds()) && CollectionUtils.isEmpty(dataPermInfo.getStationIds())) {
                if (Boolean.FALSE.equals(dataPermInfo.getHimself())) {
                    return prefix + " (1 = 2)";
                } else {
                    return prefix + " (" + alias + ".create_by = " + CurrentUser.getCurrent().getId() + ")";
                }
            }

            StringBuilder sql = new StringBuilder();
            sql.append(prefix).append(" ").append("(");
            if (null == dataPermInfo.getHimself()) {
                sql.append(alias).append(".").append("create_by = ").append(CurrentUser.getCurrent().getId()).append(" OR ");
            }

            if (CollectionUtils.isNotEmpty(dataPermInfo.getDeptIds())) {
                sql.append(alias).append(".").append("department_id IN (").append(StringUtils.join(dataPermInfo.getDeptIds(), ",")).append(") OR ");
            }

            if (CollectionUtils.isNotEmpty(dataPermInfo.getStationIds())) {
                sql.append(alias).append(".").append("station_id IN (").append(StringUtils.join(dataPermInfo.getStationIds(), ",")).append(") OR ");
            }
            sql.setLength(sql.length() - 4);
            return sql.append(")").toString();
        }
    }
}
