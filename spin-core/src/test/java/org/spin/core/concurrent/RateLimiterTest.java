package org.spin.core.concurrent;

import org.junit.jupiter.api.Test;
import org.spin.core.base.Stopwatch;
import org.spin.core.util.DateUtils;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/9/27.</p>
 *
 * @author xuweinan
 */
public class RateLimiterTest {

    static RateLimiter limiter = RateLimiter.create(1);

    public static void main(String[] args) {
        int i = 0;
        limiter.acquire();
        Uninterruptibles.sleepUninterruptibly(10L, TimeUnit.SECONDS);
        while (i < 60) {
//            boolean b = limiter.tryAcquire();
//            if (!b) {
//                Thread.sleep(100L);
//            } else {
//                System.out.println(i + "-" + DateUtils.formatDateForMillSec(new Date()));
//                ++i;
//            }
            limiter.acquire();
            System.out.println(i + "-" + DateUtils.formatDateForMillSec(new Date()));
            ++i;
        }

    }

    @Test
    void testCdl() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(0);

        countDownLatch.countDown();
        countDownLatch.await();
        countDownLatch.await();
        countDownLatch.await();
        countDownLatch.await();
        countDownLatch.await();
    }


    @Test
    public void testArray() {
        long[] intArray = new long[1000000];
        long[] longs = new long[1000000];
        int i = 0;

        long s = System.currentTimeMillis();
        while (i != 1000000) {
            intArray[i] = System.nanoTime();
            ++i;
        }
        long e = System.currentTimeMillis();
        System.out.println(e - s);

        s = System.currentTimeMillis();
        --i;
        while (i != -1) {
            if (System.nanoTime() - intArray[i] > 4_000_000) {
                break;
            }
            --i;
        }
        System.arraycopy(intArray, i, longs, 0, intArray.length - i);
        e = System.currentTimeMillis();
        System.out.println(e - s);

        System.out.println(i);
    }

    @Test
    public void testStopwatch() throws InterruptedException {
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        stopwatch.start();
        int i = 0;
        while (i < 201) {
            stopwatch.record();
            System.out.println(stopwatch.elapsedRecord(i + 1).toMillis());
            ++i;
        }
        stopwatch.stop();
        stopwatch.elapsed();

    }
}
