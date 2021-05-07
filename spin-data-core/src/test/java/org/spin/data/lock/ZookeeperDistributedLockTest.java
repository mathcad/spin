package org.spin.data.lock;


import org.junit.jupiter.api.Test;
import org.spin.core.concurrent.Async;
import org.spin.core.concurrent.LockTicket;

import java.util.concurrent.CountDownLatch;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/6/10</p>
 *
 * @author xuweinan
 * @version 1.0
 */
class ZookeeperDistributedLockTest {

    @Test
    void testZk() throws InterruptedException {
        ZookeeperDistributedLock zkLock = new ZookeeperDistributedLock("192.168.40.149");
        LockTicket lock = zkLock.lock("test");
        CountDownLatch latch = new CountDownLatch(1);

        if (lock.isSuccess()) {
            System.out.println("主线程加锁成功");
            Async.run(() -> {
                System.out.println("子线程开始");
                long c = System.currentTimeMillis();
                LockTicket child = zkLock.lock("test", 0, 100, 100);
                System.out.println("上锁耗时" + (System.currentTimeMillis() - c));
                if (child.isSuccess()) {
                    System.out.println("子线程加锁成功");
                    Thread.sleep(5000L);
                    child.close();
                } else {
                    System.out.println("子线程加锁失败");
                }
                latch.countDown();
            });
            Thread.sleep(1000L);
            lock.close();
        } else {
            System.out.println("主线程加锁失败");
        }

        latch.await();
        System.out.println("finish");
    }
}
