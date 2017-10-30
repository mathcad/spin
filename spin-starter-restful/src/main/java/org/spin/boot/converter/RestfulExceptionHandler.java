package org.spin.boot.converter;

/**
 * 异常处理器接口
 * <p>Created by xuweinan on 2017/9/27.</p>
 *
 * @author xuweinan
 */
public interface RestfulExceptionHandler {

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

    /**
     * 排序号，低排序号具有更高的优先级
     *
     * @return 排序号
     */
    int getOrder();
}
