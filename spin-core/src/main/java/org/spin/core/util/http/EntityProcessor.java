package org.spin.core.util.http;

import org.apache.http.HttpEntity;

/**
 * 请求结果处理接口，处理http请求返回的HttpEntity结果
 * <p>Created by xuweinan on 2018/4/9.</p>
 *
 * @param <T> 处理后的数据类型
 * @author xuweinan
 */
@FunctionalInterface
public interface EntityProcessor<T> {
    T process(HttpEntity entity);
}
