package org.spin.data.lock;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.concurrent.DistributedLock;
import org.spin.core.throwable.SimplifiedException;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 基于Zookeeper的分布式锁实现
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/6/10</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class ZookeeperDistributedLock implements DistributedLock {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperDistributedLock.class);

    /**
     * 分布式锁的根目录
     */
    private static final String ROOT_PATH = "/spin_zookeeper_locks";

    /**
     * 锁名称，创建的顺序节点例如都以lock-开头，这样便于过滤无关节点。这样创建后的节点类似：lock-00000001，lock-000000002
     */
    private static final String LOCK_NAME = "lock-";

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_COUNT = 10;

    /**
     * 当前线程上的锁实例
     */
    private ThreadLocal<Map<String, String>> currentLock = ThreadLocal.withInitial(HashMap::new);

    private final ZkClient client;

    public ZookeeperDistributedLock(ZkClient client) {
        this.client = client;
        if (!client.exists(ROOT_PATH)) {
            client.createPersistent(ROOT_PATH);
        }
    }

    @Override
    public boolean lock(String key, long expire, int retryTimes, long sleepMillis) {
        return internalLock(key, expire, retryTimes);
    }

    @Override
    public boolean releaseLock(String key) {
        String removeKey = currentLock.get().remove(key);
        if (null != removeKey) {
            return client.delete(removeKey);
        }
        return true;
    }

    private String createLockNode(String key) {
        String path = ROOT_PATH + "/" + key;
        if (!client.exists(path)) {
            client.createPersistent(path, true);
        }

        return client.createEphemeralSequential(path + "/" + LOCK_NAME, null);
    }

    /**
     * 获取锁的核心方法
     *
     * @param key          锁的key
     * @param lockNode     锁实例
     * @param startMillis  获取所的开始时间
     * @param millisToWait 等待时间
     * @return 是否获得锁
     */
    private boolean waitToLock(String key, String lockNode, long startMillis, Long millisToWait, int retryTimes) {

        boolean haveTheLock = false;
        boolean needRelease = false;

        int retryCnt = retryTimes > 0 ? retryTimes : 0;
        try {
            while (!haveTheLock && retryCnt-- > -1) {
                //该方法实现获取locker节点下的所有顺序节点，并且从小到大排序
                List<String> children = getSortedChildren(key);
                String sequenceNodeName = lockNode.substring(ROOT_PATH.length() + key.length() + 2);

                //计算刚才客户端创建的顺序节点在locker的所有子节点中排序位置，如果是排序为0，则表示获取到了锁
                int lockIndex = children.indexOf(sequenceNodeName);

                /*如果在getSortedChildren中没有找到之前创建的[临时]顺序节点，这表示可能由于网络闪断而导致
                 *Zookeeper认为连接断开而删除了我们创建的节点，此时需要抛出异常，让上一级去处理
                 *上一级的做法是捕获该异常，并且执行重试指定的次数 见后面的 attemptLock方法 */
                if (lockIndex < 0) {
                    throw new ZkNoNodeException("节点没有找到: " + sequenceNodeName);
                }

                //如果当前客户端创建的节点在locker子节点列表中位置大于0，表示其它客户端已经获取了锁
                //此时当前客户端需要等待其它客户端释放锁，
                boolean isGetTheLock = lockIndex == 0;

                //如何判断其它客户端是否已经释放了锁？从子节点列表中获取到比自己次小的哪个节点，并对其建立监听
                String lockToWait = isGetTheLock ? null : children.get(lockIndex - 1);

                if (isGetTheLock) {
                    haveTheLock = true;
                } else {
                    //如果次小的节点被删除了，则表示当前客户端的节点应该是最小的了，所以使用CountDownLatch来实现等待
                    final CountDownLatch latch = new CountDownLatch(1);

                    final IZkDataListener previousListener = new IZkDataListener() {

                        //次小节点删除事件发生时，让countDownLatch结束等待
                        //此时还需要重新让程序回到while，重新判断一次！
                        @Override
                        public void handleDataDeleted(String dataPath) {
                            latch.countDown();
                        }

                        @Override
                        public void handleDataChange(String dataPath, Object data) {
                            // do nothing
                        }
                    };

                    try {
                        //如果节点不存在会出现异常
                        client.subscribeDataChanges(lockToWait, previousListener);

                        if (millisToWait != null) {
                            millisToWait -= (System.currentTimeMillis() - startMillis);
                            startMillis = System.currentTimeMillis();
                            if (millisToWait <= 0) {
                                needRelease = true;
                                break;
                            }

                            latch.await(millisToWait, TimeUnit.MICROSECONDS);
                        } else {
                            latch.await();
                        }

                    } catch (ZkNoNodeException e) {
                        // ignore
                    } finally {
                        client.unsubscribeDataChanges(lockToWait, previousListener);
                    }
                }
            }
        } catch (Exception e) {
            //发生异常需要删除节点
            needRelease = true;
            logger.error("分布式锁获取失败", e);
            throw new SimplifiedException("分布式锁获取失败", e);
        } finally {
            //如果需要删除节点
            if (needRelease) {
                releaseLock(lockNode);
            }
        }
        return haveTheLock;
    }


    private long getLockNodeNumber(String str) {
        String num = str.substring(str.lastIndexOf('/') + 6);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < num.length(); i++) {
            if (num.charAt(i) != '0') {
                sb.append(num.charAt(i));
            }
        }

        return Long.parseLong(sb.toString());
    }


    /**
     * 获取指定键下排好序的锁等待列表
     *
     * @param key 锁的key
     * @return 排序后的等待列表
     */
    private List<String> getSortedChildren(String key) {
        String path = ROOT_PATH + "/" + key;
        try {
            List<String> children = client.getChildren(path);
            children.sort(Comparator.comparing(this::getLockNodeNumber));
            return children;

        } catch (ZkNoNodeException e) {
            logger.debug("指定的锁[{}]不存在，即将创建", key);
            client.createPersistent(path, true);
            return Collections.emptyList();
        }
    }

    /**
     * 尝试获取锁
     *
     * @param key           锁的key
     * @param timeoutMillis 超时时间
     * @return 锁实例id
     */
    private String attemptLock(String key, long timeoutMillis, int retryTimes) {
        final long startMillis = System.currentTimeMillis();
        final Long millisToWait = timeoutMillis > 0 ? timeoutMillis : null;

        String lockNode = null;
        boolean hasTheLock = false;
        boolean isDone = false;
        int retryCount = 0;

        //网络闪断需要重试一试
        while (!isDone) {
            isDone = true;

            try {
                //createLockNode用于在locker（basePath持久节点）下创建客户端要获取锁的[临时]顺序节点
                lockNode = createLockNode(key);

                /*
                 * 该方法用于判断自己是否获取到了锁，即自己创建的顺序节点在locker的所有子节点中是否最小
                 * 如果没有获取到锁，则等待其它客户端锁的释放，并且稍后重试直到获取到锁或者超时
                 */
                hasTheLock = waitToLock(key, lockNode, startMillis, millisToWait, retryTimes);

            } catch (ZkNoNodeException e) {
                if (retryCount++ < MAX_RETRY_COUNT) {
                    isDone = false;
                } else {
                    throw e;
                }
            }
        }

        if (hasTheLock) {
            return lockNode;
        }

        return null;
    }

    private boolean internalLock(String key, long timeoutMillis, int retryTimes) {
        //如果ourLockPath不为空则认为获取到了锁，具体实现细节见attemptLock的实现
        currentLock.get().put(key, attemptLock(key, timeoutMillis, retryTimes));
        return currentLock.get().get(key) != null;
    }
}
