package org.spin.mybatis.handler;

import org.apache.ibatis.reflection.MetaObject;
import org.spin.cloud.vo.CurrentUser;
import org.spin.cloud.vo.SessionEmpInfo;
import org.spin.core.util.CollectionUtils;

/**
 * description 数据权限信息 字段自动填充
 *
 * @author wangy QQ 837195190
 * <p>Created by wangy on 2020/4/2.</p>
 */
public class PermissionDataMetaObjectHandler extends MybatisMetaObjectHandler {


    @Override
    public void insertFill(MetaObject metaObject) {
        if (null == CurrentUser.getCurrent()) {
            return;
        }
        SessionEmpInfo empInfo = CurrentUser.getCurrentEmpInfo();
        if (null != empInfo) {
            setFieldValByName("departmentId", CollectionUtils.first(empInfo.getDepts()), metaObject);
            setFieldValByName("stationId", CollectionUtils.first(empInfo.getStations()), metaObject);
            setFieldValByName("customOrganId", CollectionUtils.first(empInfo.getOrgans()), metaObject);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // do nothing
    }


}
