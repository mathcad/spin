package org.spin.core.concurrent;

import org.spin.core.util.DateUtils;

import java.util.Date;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/9/27.</p>
 *
 * @author xuweinan
 */
public class RateLimiterTest {

    static RateLimiter limiter = RateLimiter.create(10);

    public static void main(String[] args) throws InterruptedException {
        int i = 0;
        limiter.acquire();
        Thread.sleep(1000);
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
}
