package org.spin.core.concurrent;

import org.spin.core.throwable.SpinException;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/9/28</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class PulseRateLimiter extends RateLimiter {

    private long[] worker;
    private long[] swap;

    private long timeWindow;

    private int rate;
    private int size;
    private AtomicInteger cur;
    private int threshold;


    PulseRateLimiter(SleepingStopwatch stopwatch, int rate, int timeWindow, TimeUnit timeUnit) {
        super(stopwatch);
        cur = new AtomicInteger(0);
        this.rate = rate;
        size = Math.max(rate, 100);
        threshold = (int) (size * 0.75);
        worker = new long[size];
        swap = new long[size];

        switch (timeUnit) {
            case MILLISECONDS:
                this.timeWindow = timeWindow;
                break;
            case MICROSECONDS:
                this.timeWindow = timeWindow * 100L;
                break;
            case SECONDS:
                this.timeWindow = timeWindow * 1_000L;
                break;
            case MINUTES:
                this.timeWindow = timeWindow * 60_000L;
                break;
            case HOURS:
                this.timeWindow = timeWindow * 3_600_000L;
                break;
            case DAYS:
                this.timeWindow = timeWindow * 86_400_000L;
                break;
            default:
                throw new SpinException("不支持的时间单位" + timeUnit.name());
        }
    }

    /**
     * 垃圾回收
     */
    private synchronized void gc() {
        if (cur.intValue() < threshold) {
            return;
        }
        int i = windowIdx();

        cur = new AtomicInteger(cur.intValue() - i + 1);
        System.arraycopy(worker, i, swap, 0, cur.intValue());
        long[] tmp = worker;
        worker = swap;
        swap = tmp;
        Arrays.fill(swap, 0L);
    }

    /**
     * 计算当前时间窗口的起始索引
     *
     * @return 起始索引
     */
    private int windowIdx() {
        int i = 0;
        while (i != cur.intValue()) {
            if (System.currentTimeMillis() - worker[i] < timeWindow) {
                break;
            } else {
                ++i;
            }
        }
        return i;
    }

    /**
     * 计算指定时刻获取指定数量的令牌需要等待的时间
     *
     * @param time    时刻
     * @param permits 令牌数量
     * @return 等待时间(毫秒)
     */
    private long waitTime(long time, int permits) {
        if (cur.intValue() + permits < rate) {
            Arrays.fill(worker, cur.intValue(), cur.addAndGet(permits), time);
            return 0L;
        } else {
            int i = windowIdx();
            if (cur.intValue() - i < permits) {
                Arrays.fill(worker, cur.intValue(), cur.addAndGet(permits), time);
                return 0L;
            } else {
                return time - worker[cur.intValue() - i + permits];
            }
        }
    }

    @Override
    public double acquire() {
        if (cur.intValue() < rate) {
            worker[cur.getAndIncrement()] = System.currentTimeMillis();
            return 0D;
        } else {
            gc();
            if (cur.intValue() < rate) {
                worker[cur.getAndIncrement()] = System.currentTimeMillis();
                return 0D;
            } else {
                int i = windowIdx();
            }

        }
        return 0;
    }

    @Override
    public double acquire(int permits) {
        return super.acquire(permits);
    }

    @Override
    public boolean tryAcquire(long timeout, TimeUnit unit) {
        return super.tryAcquire(timeout, unit);
    }

    @Override
    public boolean tryAcquire(int permits) {
        return super.tryAcquire(permits);
    }

    @Override
    public boolean tryAcquire() {
        return super.tryAcquire();
    }

    @Override
    public boolean tryAcquire(int permits, long timeout, TimeUnit unit) {
        return super.tryAcquire(permits, timeout, unit);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    void doSetRate(double permitsPerSecond, long nowMicros) {

    }

    @Override
    double doGetRate() {
        return rate;
    }

    @Override
    long queryEarliestAvailable(long nowMicros) {
        return 0;
    }

    @Override
    long reserveEarliestAvailable(int permits, long nowMicros) {
        return 0;
    }
}
