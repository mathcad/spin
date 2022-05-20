package org.spin.mybatis.query;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.spin.data.rs.AffectedRows;

/**
 * MyBatis条件更新执行器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/1/4</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public interface MappedUpdateExecutor<E, M extends BaseMapper<E>> {

    M repo();

    Wrapper<E> getUpdate();


    /**
     * 执行条件更新
     *
     * @return 影响行数
     */
    default AffectedRows exec() {
        return AffectedRows.of(repo().update(null, getUpdate()));
    }
}
