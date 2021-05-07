package org.spin.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.spin.core.util.ArrayUtils;
import org.spin.mybatis.query.LambdaQueryExecutor;

/**
 * Mybatis Plus的Mapper扩展
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/1/4</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public interface BaseExMapper<T> extends BaseMapper<T> {

    @SuppressWarnings("unchecked")
    default LambdaQueryExecutor<T> query(T... ignore) {
        Class<T> type = ArrayUtils.resolveArrayCompType(ignore);
        LambdaQueryExecutor<T> lambda = new LambdaQueryExecutor<>(type);
        lambda.setRepo(this);
        return lambda;
    }
}
