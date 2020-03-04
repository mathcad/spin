package org.spin.data.lock;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.core.concurrent.DistributedLock;
import org.spin.core.function.ExceptionalHandler;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.throwable.SpinException;
import org.spin.data.throwable.ZookeeperException;

import javax.security.auth.login.Configuration;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * 基于Zookeeper的分布式锁实现
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/3/3</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class ZookeeperDistributedLock implements DistributedLock, Watcher, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperDistributedLock.class);
    private static final int DEFAULT_SESSION_TIMEOUT = 30000;
    private static final ConcurrentHashMap<String, Consumer<String>> DELETE_LISTENER_MAP = new ConcurrentHashMap<>();
    private static final String JAVA_LOGIN_CONFIG_PARAM = "java.security.auth.login.config";
    private static final String ZK_SASL_CLIENT = "zookeeper.sasl.client";
    private static final String ZK_LOGIN_CONTEXT_NAME_KEY = "zookeeper.sasl.clientconfig";

    /**
     * 分布式锁的根目录
     */
    private static final String ROOT_PATH = "/spin_zookeeper_locks";
    private static final char DELIMITER = '/';

    /**
     * 锁名称，创建的顺序节点都以lock-开头，这样便于过滤无关节点。这样创建后的节点类似：lock-00000001，lock-000000002
     */
    private static final String LOCK_NAME = "lock-";

    private final String zkServers;
    private final int sessionTimeOut;
    private final long operationRetryTimeoutInMillis;
    private boolean zkSaslEnabled = isZkSaslEnabled();

    private ZooKeeper zooKeeper;
    private final ReentrantLock zookeeperLock = new ReentrantLock();

    private Event.KeeperState currentState;
    private final ZkEventLock eventLock = new ZkEventLock();

    /**
     * 当前线程上的锁实例
     */
    private ThreadLocal<Map<String, String>> currentLock = ThreadLocal.withInitial(HashMap::new);

    public ZookeeperDistributedLock(String zkServers) {
        this(zkServers, Integer.MAX_VALUE);
    }

    public ZookeeperDistributedLock(String zkServers, int connectionTimeOut) {
        this(zkServers, DEFAULT_SESSION_TIMEOUT, connectionTimeOut, -1L);
    }

    public ZookeeperDistributedLock(String zkServers, int sessionTimeOut, int connectionTimeOut, long operationTimeoutInMillis) {
        this.zkServers = zkServers;
        this.sessionTimeOut = sessionTimeOut;
        this.operationRetryTimeoutInMillis = operationTimeoutInMillis;

        connect();

        boolean waitSuccessful = waitUntilConnected(connectionTimeOut);
        if (!waitSuccessful) {
            throw new SpinException("连接超时");
        }

        createPersistentIfNotExist(ROOT_PATH);
    }

    @Override
    public boolean lock(String key, long expire, int retryTimes, long sleepMillis) {
        return attemptLock(key, expire, retryTimes);
    }

    @Override
    public boolean releaseLock(String key) {
        String removeKey = currentLock.get().remove(key);
        if (null != removeKey) {
            try {
                retryUntilConnected(() -> zooKeeper.delete(removeKey, -1));
                return true;
            } catch (NoSuchElementException e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void process(WatchedEvent event) {
        logger.debug("Received event: {}", event);
        boolean stateChanged = event.getPath() == null;
        boolean nodeDeleted = event.getType() == Event.EventType.NodeDeleted;
        eventLock.lock();
        try {
            // We might have to install child change event listener if a new node was created
            if (stateChanged) {
                processStateChanged(event);
            }
            if (nodeDeleted) {
                processNodeDeleted(event);
            }
        } finally {
            if (stateChanged) {
                eventLock.getStateChangedCondition().signalAll();
            }
            eventLock.unlock();
            logger.debug("Leaving process event");
        }
    }

    @Override
    public void close() {
        zookeeperLock.lock();
        try {
            if (zooKeeper != null) {
                logger.debug("Closing ZooKeeper connected to: {}", zkServers);
                zooKeeper.close();
                zooKeeper = null;
            }
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        } finally {
            zookeeperLock.unlock();
        }
    }

    /**
     * 尝试获取锁
     *
     * @param key    锁的key
     * @param expire 超时时间
     * @return 是否获得锁
     */
    private boolean attemptLock(String key, long expire, int retryTimes) {
        final long startMillis = System.currentTimeMillis();
        final Long millisToWait = expire > 0 ? expire : null;

        boolean hasTheLock = false;
        boolean isDone = false;

        //网络闪断需要重试一试
        while (!isDone) {
            isDone = true;

            try {
                //createLockNode用于在locker（basePath持久节点）下创建客户端要获取锁的[临时]顺序节点
                String lockNode = createLockNode(key);
                currentLock.get().put(key, lockNode);
                /*
                 * 该方法用于判断自己是否获取到了锁，即自己创建的顺序节点在locker的所有子节点中是否最小
                 * 如果没有获取到锁，则等待其它客户端锁的释放，并且稍后重试直到获取到锁或者超时
                 */
                hasTheLock = waitToLock(key, lockNode, startMillis, millisToWait);

            } catch (NoSuchElementException e) {
                if (retryTimes-- > -1) {
                    isDone = false;
                } else {
                    throw e;
                }
            }
        }

        return hasTheLock;
    }

    /**
     * 在指定的key下创建自增的lock实例节点
     *
     * @param key 锁的key
     * @return 锁实例
     */
    private String createLockNode(String key) {
        String path = ROOT_PATH + DELIMITER + key;
        createPersistentIfNotExist(path);

        return retryUntilConnected(() -> zooKeeper.create(path + DELIMITER + LOCK_NAME, null,
            ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL));
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
    private boolean waitToLock(String key, String lockNode, long startMillis, Long millisToWait) {

        boolean haveTheLock = false;
        boolean needRelease = false;

        try {
            while (!haveTheLock) {
                //该方法实现获取locker节点下的所有顺序节点，并且从小到大排序
                List<String> children = getSortedChildren(key);
                String sequenceNodeName = lockNode.substring(ROOT_PATH.length() + key.length() + 2);

                //计算刚才客户端创建的顺序节点在locker的所有子节点中排序位置，如果是排序为0，则表示获取到了锁
                int lockIndex = children.indexOf(sequenceNodeName);

                /*如果在getSortedChildren中没有找到之前创建的[临时]顺序节点，这表示可能由于网络闪断而导致
                 *Zookeeper认为连接断开而删除了我们创建的节点，此时需要抛出异常，让上一级去处理
                 *上一级的做法是捕获该异常，并且执行重试指定的次数 见后面的 attemptLock方法 */
                if (lockIndex < 0) {
                    throw new NoSuchElementException("节点没有找到: " + sequenceNodeName);
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

                    try {
                        //次小节点删除事件发生时，让countDownLatch结束等待
                        //此时还需要重新让程序回到while，重新判断一次
                        watchOnDelete(ROOT_PATH + DELIMITER + key + DELIMITER + lockToWait, path -> latch.countDown());

                        if (millisToWait != null) {
                            millisToWait -= (System.currentTimeMillis() - startMillis);
                            startMillis = System.currentTimeMillis();
                            if (millisToWait <= 0) {
                                needRelease = true;
                                break;
                            }
                            if (!latch.await(millisToWait, TimeUnit.MILLISECONDS)) {
                                unwatchOnDelete(ROOT_PATH + DELIMITER + key + DELIMITER + lockToWait);
                                needRelease = true;
                                break;
                            }
                        } else {
                            latch.await();
                        }

                    } catch (NoSuchElementException e) {
                        // ignore
                    }
                }
            }
        } catch (Exception e) {
            //发生异常需要删除节点
            needRelease = true;
            throw new SimplifiedException("分布式锁获取失败", e);
        } finally {
            //如果需要删除节点
            if (needRelease) {
                releaseLock(key);
            }
        }
        return haveTheLock;
    }

    /**
     * 获取指定键下排好序的锁等待列表
     *
     * @param key 锁的key
     * @return 排序后的等待列表
     */
    private List<String> getSortedChildren(String key) {
        String path = ROOT_PATH + DELIMITER + key;
        try {
            List<String> allNodes = retryUntilConnected(() -> zooKeeper.getChildren(path, false));
            allNodes.sort(Comparator.comparing(this::getLockNodeNumber));
            return allNodes;
        } catch (NoSuchElementException e) {
            logger.debug("指定的锁[{}]不存在，即将创建", key);
            createPersistentIfNotExist(path);
            return Collections.emptyList();
        }
    }

    /**
     * 从锁实例名称中获取编号
     *
     * @param nodePath 锁实例全名
     * @return 编号
     */
    private long getLockNodeNumber(String nodePath) {
        String num = nodePath.substring(nodePath.lastIndexOf(DELIMITER) + 6);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < num.length(); i++) {
            if (num.charAt(i) != '0' || i == num.length() - 1) {
                sb.append(num.charAt(i));
            }
        }

        return Long.parseLong(sb.toString());
    }

    private <T> T retryUntilConnected(Callable<T> callable) {
        final long operationStartTime = System.currentTimeMillis();
        int executeTimes = 0;
        while (true) {
            ++executeTimes;
            try {
                return callable.call();
            } catch (KeeperException.ConnectionLossException
                | KeeperException.SessionExpiredException e) {
                Thread.yield();
                waitForRetry();
            } catch (KeeperException.OperationTimeoutException
                | KeeperException.RequestTimeoutException ignore) {
                // need to retry
            } catch (KeeperException.NoNodeException e) {
                throw new NoSuchElementException("Node not existo");
            } catch (KeeperException e) {
                throw new ZookeeperException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SpinException(ErrorCode.with(80, "InterruptedException"), "Current transaction was interrupted, init operation failed: " + Thread.currentThread().getName(), e);
            } catch (Exception e) {
                throw new SpinException(e);
            }
            if (executeTimes > 4) {
                throw new SpinException(ErrorCode.with(82, "重试次数超过最大值"), "Operation cannot be retried because of retried 5 times(max 5)");
            }

            if (executeTimes > 1 && operationRetryTimeoutInMillis > 0 && (System.currentTimeMillis() - operationStartTime) >= operationRetryTimeoutInMillis) {
                throw new SpinException(ErrorCode.with(83, "逻辑执行超时"), "Operation cannot be retried because of retry timeout (" + operationRetryTimeoutInMillis + " milli seconds)");
            }
        }
    }

    private void retryUntilConnected(ExceptionalHandler<? extends Exception> handler) {
        retryUntilConnected(() -> {
            handler.handle();
            return true;
        });
    }

    private void waitForRetry() {
        waitUntilConnected(operationRetryTimeoutInMillis > 0 ? operationRetryTimeoutInMillis : Integer.MAX_VALUE);
    }

    private boolean waitUntilConnected(long time) {
        if (zkSaslEnabled) {
            return waitForKeeperState(Event.KeeperState.SaslAuthenticated, time);
        } else {
            return waitForKeeperState(Event.KeeperState.SyncConnected, time);
        }
    }

    private boolean waitForKeeperState(Event.KeeperState keeperState, long time) {
        Date timeout = new Date(System.currentTimeMillis() + TimeUnit.MILLISECONDS.toMillis(time));

        logger.info("Waiting for keeper state: {}", keeperState);
        try {
            eventLock.lockInterruptibly();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SpinException("Current thread was interrupted: " + Thread.currentThread().getName());
        }
        try {
            boolean stillWaiting = true;
            while (currentState != keeperState) {
                if (!stillWaiting) {
                    return false;
                }
                stillWaiting = eventLock.getStateChangedCondition().awaitUntil(timeout);
                if (currentState == Watcher.Event.KeeperState.AuthFailed && zkSaslEnabled) {
                    throw new SpinException("Authentication failure");
                }
            }
            logger.debug("State is :{}", currentState);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SpinException("Current thread was interrupted: " + Thread.currentThread().getName());
        } finally {
            eventLock.unlock();
        }
    }

    private void processNodeDeleted(WatchedEvent event) {
        String path = event.getPath();
        logger.info("zookeeper node deleted ({})", path);
        if (DELETE_LISTENER_MAP.containsKey(path)) {
            DELETE_LISTENER_MAP.remove(path).accept(path);
        }
    }

    private void processStateChanged(WatchedEvent event) {
        logger.info("zookeeper state changed ({})", event.getState());
        setCurrentState(event.getState());
        if (event.getState() == Event.KeeperState.Expired) {
            try {
                reconnect();
            } catch (final Exception e) {
                logger.info("Unable to re-establish connection.", e);
            }
        }
    }

    private void setCurrentState(Event.KeeperState currentState) {
        eventLock.lock();
        try {
            this.currentState = currentState;
        } finally {
            eventLock.unlock();
        }
    }

    private void reconnect() {
        eventLock.lock();
        try {
            close();
            connect();
        } finally {
            eventLock.unlock();
        }
    }

    private void connect() {
        zookeeperLock.lock();
        try {
            if (zooKeeper != null) {
                throw new IllegalStateException("zk client has already been started");
            }
            try {
                logger.debug("Creating new ZookKeeper instance to connect to {}.", zkServers);
                zooKeeper = new ZooKeeper(zkServers, sessionTimeOut, this);
            } catch (IOException e) {
                throw new SpinException(ErrorCode.NETWORK_EXCEPTION, "Unable to connect to " + zkServers, e);
            }
        } finally {
            zookeeperLock.unlock();
        }
    }

    private boolean isZkSaslEnabled() {
        boolean isSecurityEnabled = false;
        boolean saslEnabled = Boolean.parseBoolean(System.getProperty(ZK_SASL_CLIENT, "true"));
        String zkLoginContextName = System.getProperty(ZK_LOGIN_CONTEXT_NAME_KEY, "Client");

        if (!saslEnabled) {
            logger.warn("Client SASL has been explicitly disabled with " + ZK_SASL_CLIENT);
            return false;
        }

        String loginConfigFile = System.getProperty(JAVA_LOGIN_CONFIG_PARAM);
        if (loginConfigFile != null && loginConfigFile.length() > 0) {
            logger.info("JAAS File name: {}", loginConfigFile);
            File configFile = new File(loginConfigFile);
            if (!configFile.canRead()) {
                throw new IllegalArgumentException("File " + loginConfigFile + "cannot be read.");
            }

            Configuration loginConf = Configuration.getConfiguration();
            isSecurityEnabled = loginConf.getAppConfigurationEntry(zkLoginContextName) != null;
        }
        return isSecurityEnabled;
    }

    private void createPersistentIfNotExist(final String path) {
        retryUntilConnected(() -> {
            if (zooKeeper.exists(path, false) == null) {
                ArrayList<ACL> acl = ZooDefs.Ids.OPEN_ACL_UNSAFE;
                try {
                    zooKeeper.create(path, null, acl, CreateMode.PERSISTENT);
                } catch (KeeperException.NoNodeException e) {
                    int idx = path.indexOf(DELIMITER, 21);
                    // 创建parent
                    while (idx > 0) {
                        try {
                            zooKeeper.create(path.substring(0, idx), null, acl, CreateMode.PERSISTENT);
                        } catch (KeeperException.NodeExistsException ignore) {
                            // do nothing
                        }
                        idx = path.indexOf(DELIMITER, idx + 1);
                        if (idx < 0) {
                            zooKeeper.create(path, null, acl, CreateMode.PERSISTENT);
                            return;
                        }
                    }
                } catch (KeeperException.NodeExistsException ignore) {
                    // do nothing
                }
            }
        });
    }

    private void watchOnDelete(String path, Consumer<String> handler) {
        if (DELETE_LISTENER_MAP.containsKey(path)) {
            throw new SpinException("该路径已经被其他锁监听");
        }
        DELETE_LISTENER_MAP.put(path, handler);
        retryUntilConnected(() -> zooKeeper.exists(path, true));
    }

    private void unwatchOnDelete(String path) {
        DELETE_LISTENER_MAP.remove(path);
    }

    private static class ZkEventLock extends ReentrantLock {

        private static final long serialVersionUID = -311494924977994647L;

        private final Condition stateChangedCondition = newCondition();


        /**
         * This condition will be signaled if a zookeeper event was processed and the event contains a state change
         * (connected, disconnected, session expired, etc ...).
         *
         * @return the condition.
         */
        public Condition getStateChangedCondition() {
            return stateChangedCondition;
        }

    }
}
