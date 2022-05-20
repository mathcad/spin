package org.spin.core.concurrent;

/**
 * 滑动窗口限流器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2022/5/19</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public interface SlidingWindow {

    /**
     * 获取令牌, 超出流控时将会阻塞直至满足限流窗口
     *
     * @throws InterruptedException 线程中断时抛出
     */
    void acquire() throws InterruptedException;

    /**
     * 尝试或取令牌, 超出流控时将返回false
     *
     * @return 是否成功获取令牌
     */
    boolean tryAcquire();

    void resetPeriod(long period);
}
