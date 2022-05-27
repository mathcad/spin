package org.spin.core.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * 滑动窗口限流器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2022/5/19</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class DefaultSlidingWindow implements SlidingWindow {

    private final long[] window;
    private final int windowSize;
    private int head;
    private long period;

    public DefaultSlidingWindow(int windowSize) {
        this(windowSize, 1000L);
    }

    public DefaultSlidingWindow(int windowSize, long periodInMillis) {
        this.windowSize = windowSize;
        this.window = new long[windowSize];
        this.period = periodInMillis;
    }

    @Override
    public void acquire() throws InterruptedException {
        long time = appendTimestamp(System.currentTimeMillis());
        if (time > 0) {
            TimeUnit.MILLISECONDS.sleep(time);
        }
    }

    @Override
    public boolean tryAcquire() {
        return appendTimestampIfPossible(System.currentTimeMillis()) <= 0L;
    }

    @Override
    public void resetPeriod(long periodInMillis) {
        this.period = periodInMillis;
    }

    private synchronized long appendTimestamp(long timestamp) {
        long waitTime = period - timestamp + window[head];
        if (waitTime > 0L) {
            timestamp = waitTime + timestamp;
        }
        window[head] = timestamp;
        head = ++head % windowSize;
        return waitTime;
    }

    private synchronized long appendTimestampIfPossible(long timestamp) {
        long waitTime = period - timestamp + window[head];
        if (waitTime <= 0L) {
            window[head] = timestamp;
            head = ++head % windowSize;
        }
        return waitTime;
    }
}
