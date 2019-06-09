package org.spin.common.db.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.spin.common.vo.CurrentUser;

import java.time.LocalDateTime;

/**
 * description
 *
 * @author wangy QQ 837195190
 * <p>Created by wangy on 2018/10/25.</p>
 */
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        setFieldValByName("createTime", LocalDateTime.now(), metaObject);
        setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        CurrentUser currentUser = CurrentUser.getCurrent();

        if (null != currentUser) {
            setFieldValByName("createBy", currentUser.getId(), metaObject);
            setFieldValByName("updateBy", currentUser.getId(), metaObject);
            setFieldValByName("createUsername", currentUser.getName(), metaObject);
            setFieldValByName("updateUsername", currentUser.getName(), metaObject);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        CurrentUser currentUser = CurrentUser.getCurrent();
        if (null != currentUser) {
            setFieldValByName("updateBy", currentUser.getId(), metaObject);
            setFieldValByName("updateUsername", currentUser.getName(), metaObject);
        }
    }
}
