package org.spin.web.handler;

import org.springframework.core.Ordered;

/**
 * RequestResponseBody情况下的返回结果修改器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/4/20</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public interface RequestResponseBodyModifier extends Ordered {

    /**
     * 当前返回对象是否受支持
     *
     * @param returnValue RequestResponseBody的结果
     * @return 是否支持
     */
    default boolean supported(Object returnValue) {
        return true;
    }

    /**
     * 当前supported返回True时的修改逻辑
     *
     * @param returnValue RequestResponseBody的结果
     * @return 处理以后的返回值
     */
    Object modify(Object returnValue);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
