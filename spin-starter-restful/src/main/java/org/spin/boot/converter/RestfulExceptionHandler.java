package org.spin.boot.converter;

import org.spin.core.trait.Order;

/**
 * 异常处理器接口
 * <p>Created by xuweinan on 2017/9/27.</p>
 *
 * @author xuweinan
 */
public interface RestfulExceptionHandler extends Order {

    /**
     * 支持的异常类型
     *
     * @return 异常类型
     */
    Class<? extends Throwable> getExceptionCls();

    /**
     * 处理逻辑
     *
     * @param throwable 异常
     * @return 处理结果
     */
    String handler(Throwable throwable);
}
