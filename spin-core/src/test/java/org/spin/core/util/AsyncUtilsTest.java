package org.spin.core.util;

import org.junit.jupiter.api.Test;
import org.spin.core.concurrent.Uninterruptibles;
import org.spin.core.throwable.SpinException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/2/19</p>
 *
 * @author xuweinan
 * @version 1.0
 */
class AsyncUtilsTest {
//    private static final Thread BACK_STATIS_WORKER;

    static {
//        BACK_STATIS_WORKER = new Thread(() -> {
//            AsyncUtils.initThreadPool("test", 20);
//            while (true) {
//                System.out.println(AsyncUtils.statistic().get(0).toString());
//                try {
//                    Thread.sleep(200L);
//                } catch (InterruptedException ignore) {
//                    // do nothing
//                }
//            }
//        });
//        BACK_STATIS_WORKER.setName("xxxxxx");
//        BACK_STATIS_WORKER.start();
    }

    AtomicInteger atomicInteger = new AtomicInteger();

    @Test
    void testPool() {
        String name = "THREAD-" + atomicInteger.getAndIncrement();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10, 1000,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(5),
            AsyncUtils.buildFactory(name, true, null, (thread, throwable) -> {
            }),
            new ThreadPoolExecutor.AbortPolicy());

        for (int i = 0; i < 15; ++i) {
            Uninterruptibles.sleepUninterruptibly(50, TimeUnit.MILLISECONDS);
            System.out.println("poolsize: " + threadPoolExecutor.getPoolSize());
            final int id = i;
            threadPoolExecutor.execute(() -> {
                System.out.println(System.currentTimeMillis() + ":" + id + "==>" + Thread.currentThread().getName());
                Uninterruptibles.sleepUninterruptibly(id > 9 ? 1 : 2, TimeUnit.SECONDS);
            });
        }

        Uninterruptibles.sleepUninterruptibly(10, TimeUnit.SECONDS);
    }

    @Test
    void runAsync() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(13);
        Thread.sleep(10L);
        AsyncUtils.execute("test", () -> t(1, countDownLatch));
        AsyncUtils.execute("test", () -> t(2, countDownLatch));
        AsyncUtils.execute("test", () -> t(3, countDownLatch));
        AsyncUtils.execute("test", () -> t(4, countDownLatch));
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
    }

    public void t(int i, CountDownLatch countDownLatch) throws InterruptedException {
        System.out.println(i + Thread.currentThread().getName());
        Thread.sleep(5000L);
        countDownLatch.countDown();
        if (i > 6) {
            throw new SpinException("aaa");
        }
    }
}
