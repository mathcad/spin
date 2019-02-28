package org.spin.core.util;

import org.junit.jupiter.api.Test;
import org.spin.core.throwable.SimplifiedException;

import java.util.concurrent.CountDownLatch;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/2/19</p>
 *
 * @author xuweinan
 * @version 1.0
 */
class AsyncUtilsTest {
    private static final Thread BACK_STATIS_WORKER;

    static {
        BACK_STATIS_WORKER = new Thread(() -> {
            AsyncUtils.initThreadPool("test", 20);
            while (true) {
                System.out.println(AsyncUtils.statistic().get(0).toString());
                try {
                    Thread.sleep(200L);
                } catch (InterruptedException ignore) {
                    // do nothing
                }
            }
        });
        BACK_STATIS_WORKER.setName("xxxxxx");
        BACK_STATIS_WORKER.start();
    }

    @Test
    void runAsync() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(13);
        Thread.sleep(10L);
        AsyncUtils.submit("test", () -> t(1, countDownLatch));
        AsyncUtils.submit("test", () -> t(2, countDownLatch));
        AsyncUtils.submit("test", () -> t(3, countDownLatch));
        AsyncUtils.submit("test", () -> t(4, countDownLatch));
        AsyncUtils.runAsync(() -> t(5, countDownLatch));
        AsyncUtils.runAsync(() -> t(6, countDownLatch));
        AsyncUtils.runAsync(() -> t(7, countDownLatch));
        AsyncUtils.runAsync(() -> t(8, countDownLatch));
        AsyncUtils.runAsync(() -> t(9, countDownLatch));
        AsyncUtils.runAsync(() -> t(10, countDownLatch));
        AsyncUtils.runAsync(() -> t(11, countDownLatch));
        AsyncUtils.runAsync(() -> t(12, countDownLatch));
        AsyncUtils.runAsync(() -> t(13, countDownLatch));
        countDownLatch.await();
        Thread.sleep(2000L);
    }

    public void t(int i, CountDownLatch countDownLatch) throws InterruptedException {
        System.out.println(i + Thread.currentThread().getName());
        Thread.sleep(5000L);
        countDownLatch.countDown();
        if (i > 6) {
            throw new SimplifiedException("aaa");
        }
    }
}
