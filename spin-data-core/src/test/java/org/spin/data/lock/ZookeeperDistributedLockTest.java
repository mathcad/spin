package org.spin.data.lock;

import org.I0Itec.zkclient.ZkClient;
import org.junit.jupiter.api.Test;
import org.spin.core.util.AsyncUtils;

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

    void testZk() throws InterruptedException {
        ZookeeperDistributedLock lock = new ZookeeperDistributedLock("192.168.12.149");
        boolean test = lock.lock("test");
        CountDownLatch latch = new CountDownLatch(1);
        if (test) {
            System.out.println("主线程加锁成功");
            AsyncUtils.runAsync(() -> {
                System.out.println("子线程开始");
                if (lock.lock("test", 1020L)) {
                    System.out.println("子线程加锁成功");
                    Thread.sleep(5000L);
                    if (lock.releaseLock("test")) {
                        System.out.println("子线程释放锁成功");
                    } else {
                        System.out.println("子线程释放锁失败");
                    }
                } else {
                    System.out.println("子线程加锁失败");
                }
                latch.countDown();
            });
            Thread.sleep(1000L);
            if (lock.releaseLock("test")) {
                System.out.println("主线程释放锁成功");
            } else {
                System.out.println("主线程释放锁失败");
            }
        } else {
            System.out.println("主线程加锁失败");
        }
        latch.await();
    }
}
