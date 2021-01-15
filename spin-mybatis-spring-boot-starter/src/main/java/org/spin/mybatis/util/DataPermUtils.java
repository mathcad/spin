package org.spin.mybatis.util;

import org.spin.cloud.vo.CurrentUser;
import org.spin.cloud.vo.DataPermInfo;
import org.spin.core.Assert;
import org.spin.core.util.CollectionUtils;
import org.spin.mybatis.entity.AbstractDataPermEntity;

import java.util.Objects;

/**
 * 实体数据权限校验工具类
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/4/8</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public abstract class DataPermUtils {

    private DataPermUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 校验当前用户是否有指定实体的数据权限
     *
     * @param entity 实体
     * @param msg    权限校验不通过时的错误信息
     * @param <T>    实体类型参数
     * @return 实体
     */
    public static <T extends AbstractDataPermEntity<T>> T validatePerm(T entity, String msg) {
        CurrentUser currentUser = CurrentUser.getCurrentNonNull();
        DataPermInfo permInfo = currentUser.getDataPermInfo();
        if (permInfo.isHasDataLimit()) {
            if (Boolean.TRUE.equals(permInfo.getHimself())) {
                Assert.isEquals(entity.getCreateBy(), currentUser.getId(), msg);
            } else {
                if (CollectionUtils.isEmpty(permInfo.getDeptIds()) && CollectionUtils.isEmpty(permInfo.getStationIds())) {
                    Assert.notTrue(Boolean.FALSE.equals(permInfo.getHimself()), msg);
                    Assert.isEquals(entity.getCreateBy(), currentUser.getId(), msg);
                }

                if (null == permInfo.getHimself() && Objects.equals(entity.getCreateBy(), currentUser.getId())) {
                    return entity;
                }

                if (CollectionUtils.isNotEmpty(permInfo.getDeptIds())) {
                    Assert.isTrue(permInfo.getDeptIds().contains(entity.getDepartmentId()), msg);
                }

                if (CollectionUtils.isNotEmpty(permInfo.getStationIds())) {
                    Assert.isTrue(permInfo.getStationIds().contains(entity.getStationId()), msg);
                }
            }
        }

        return entity;
    }
}
