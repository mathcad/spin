package org.spin.data.lock;

import org.I0Itec.zkclient.ZkClient;
import org.junit.jupiter.api.Test;

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
    void acquire() throws Exception {
        ZkClient zkClient = new ZkClient("192.168.13.178", 2000);
        ZookeeperDistributedLock distributedLock = new ZookeeperDistributedLock(zkClient);
        distributedLock.lock("test");
        distributedLock.releaseLock("test");
    }
}
