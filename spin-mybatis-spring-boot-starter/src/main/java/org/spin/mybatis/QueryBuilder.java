package org.spin.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

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
}
