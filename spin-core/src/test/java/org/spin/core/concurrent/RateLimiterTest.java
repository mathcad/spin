package org.spin.core.concurrent;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/9/27.</p>
 *
 * @author xuweinan
 */
public class RateLimiterTest {

    static RateLimiter limiter = RateLimiter.create(3);

    public static void main(String[] args) throws InterruptedException {
        int i = 0;
        while (i < 60) {
            boolean b = limiter.tryAcquire();
            if (!b) {
                Thread.sleep(1000L);
            } else {
                System.out.println(i + "-" + System.currentTimeMillis());
                ++i;
            }
        }

    }
}
