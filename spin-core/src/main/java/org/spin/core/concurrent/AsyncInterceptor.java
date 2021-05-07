package org.spin.core.concurrent;

import org.spin.core.trait.Order;

/**
 * 异步拦截器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/4/7</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public interface AsyncInterceptor extends Order {

    default void register() {
        Async.registerInterceptor(this);
    }

    /**
     * 拦截器生效的线程池名称，ALL代表全局生效
     *
     * @return 拦截器生效的线程池名称
     */
    default String getPoolName() {
        return "ALL";
    }

    /**
     * 在异步任务开始前(发起线程上)执行
     *
     * @param context 异步上下文
     */
    default void preAsync(AsyncContext context) {
    }

    /**
     * 在异步任务开始时(异步线程上)执行
     *
     * @param context 异步上下文
     */
    default void onReady(AsyncContext context) {
    }

    /**
     * 在异步任务结束时(异步线程上)执行
     *
     * @param context 异步上下文
     */
    default void onFinish(AsyncContext context) {
    }

    /**
     * 在异步任务结束后(发起线程上)执行
     *
     * @param context 异步上下文
     */
    default void afterAsync(AsyncContext context) {
    }

}
