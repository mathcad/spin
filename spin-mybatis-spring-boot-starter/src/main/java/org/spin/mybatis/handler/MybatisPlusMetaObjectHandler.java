package org.spin.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.spin.core.session.SessionUser;

import java.time.LocalDateTime;
import java.util.List;

/**
 * mybatis 字段自动填充
 *
 * @author wangy QQ 837195190
 * <p>Created by wangy on 2018/10/25.</p>
 */
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    private final List<MybatisMetaObjectHandler> mybatisMetaObjectHandlers;

    public MybatisPlusMetaObjectHandler(List<MybatisMetaObjectHandler> mybatisMetaObjectHandlers) {
        this.mybatisMetaObjectHandlers = mybatisMetaObjectHandlers;
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        setFieldValByName("createTime", LocalDateTime.now(), metaObject);
        setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        SessionUser currentUser = SessionUser.getCurrent();

        if (null != currentUser) {
            setFieldValByName("createBy", currentUser.getId(), metaObject);
            setFieldValByName("updateBy", currentUser.getId(), metaObject);
            setFieldValByName("createUsername", currentUser.getName(), metaObject);
            setFieldValByName("updateUsername", currentUser.getName(), metaObject);
        }
        mybatisMetaObjectHandlers.forEach(it -> it.insertFill(metaObject));
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        SessionUser currentUser = SessionUser.getCurrent();
        if (null != currentUser) {
            setFieldValByName("updateBy", currentUser.getId(), metaObject);
            setFieldValByName("updateUsername", currentUser.getName(), metaObject);
        }
        mybatisMetaObjectHandlers.forEach(it -> it.updateFill(metaObject));
    }

}
