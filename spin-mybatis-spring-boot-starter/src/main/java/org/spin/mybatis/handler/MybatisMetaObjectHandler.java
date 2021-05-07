package org.spin.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;

/**
 * 元对象字段填充控制器抽象类，实现公共字段自动写入
 * <p>扩展 mybatis 字段填充</p>
 *
 * @author wangy QQ 837195190
 * <p>Created by wangy on 2020/4/2.</p>
 */
public abstract class MybatisMetaObjectHandler implements MetaObjectHandler {

    @Override
    public final boolean openInsertFill() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean openUpdateFill() {
        throw new UnsupportedOperationException();
    }
}
