package org.spin.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;
import org.spin.core.function.ExceptionalHandler;
import org.spin.core.function.FinalConsumer;
import org.spin.core.throwable.SimplifiedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAccumulator;

/**
 * 线程池工具类
 * <p>提供全局的异步调用与线程池工具</p>
 * <p>Created by xuweinan on 2019/2/19</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public abstract class AsyncUtils {
    private static final Logger logger = LoggerFactory.getLogger(AsyncUtils.class);

    private static final String COMMON_POOL_NAME = "GlobalCommon";
    private static final ThreadFactory THREAD_FACTORY = Executors.defaultThreadFactory();

    private static final Map<String, ThreadPoolExecutor> POOL_EXECUTOR_MAP = new ConcurrentHashMap<>();
    private static final Map<String, ThreadPoolInfo> THREAD_POOL_INFO_MAP = new ConcurrentHashMap<>();

    static {
        POOL_EXECUTOR_MAP.put(COMMON_POOL_NAME, new ThreadPoolExecutor(5, 10, 0L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(5),
            buildFactory(COMMON_POOL_NAME, null, null, (thread, throwable) -> {
            }),
            new ThreadPoolExecutor.CallerRunsPolicy()));
        THREAD_POOL_INFO_MAP.put(COMMON_POOL_NAME, new ThreadPoolInfo(COMMON_POOL_NAME, 5, 10, 5));
    }

    /**
     * 初始化一个指定名称的线程池
     *
     * @param name     线程池名称
     * @param poolSize 最大线程数
     */
    public static void initThreadPool(String name, int poolSize) {
        initThreadPool(name, poolSize, poolSize, 10L, -1, new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * 初始化一个指定名称的线程池
     *
     * @param name         线程池名称
     * @param corePoolSize 核心线程数
     * @param maxPoolSize  最大线程数
     * @param queueSize    阻塞队列长度
     */
    public static void initThreadPool(String name, int corePoolSize, int maxPoolSize, int queueSize) {
        initThreadPool(name, corePoolSize, maxPoolSize, 10L, queueSize, new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * 初始化一个指定名称的线程池
     *
     * @param name                     线程池名称
     * @param corePoolSize             核心线程数
     * @param maxPoolSize              最大线程数
     * @param keepAliveTimeInMs        空闲线程存活时间(毫秒)
     * @param queueSize                阻塞队列长度，负值表示无界
     * @param rejectedExecutionHandler 拒绝策略
     */
    public static void initThreadPool(String name, int corePoolSize, int maxPoolSize, long keepAliveTimeInMs, int queueSize, RejectedExecutionHandler rejectedExecutionHandler) {
        Assert.notEmpty(name, "线程池名称不能为空");
        Assert.notTrue(COMMON_POOL_NAME.equals(name), "公共线程池不允许用户创建");
        if (POOL_EXECUTOR_MAP.containsKey(name)) {
            ExecutorService executorService = POOL_EXECUTOR_MAP.remove(name);
            executorService.shutdown();
        }

        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTimeInMs,
            TimeUnit.MILLISECONDS,
            queueSize >= 0 ? new LinkedBlockingQueue<>(queueSize) : new LinkedBlockingQueue<>(),
            buildFactory(name, null, null, (thread, throwable) -> {
            }),
            rejectedExecutionHandler);

        POOL_EXECUTOR_MAP.put(name, threadPool);
        THREAD_POOL_INFO_MAP.put(name, new ThreadPoolInfo(name, corePoolSize, maxPoolSize, queueSize));
    }

    public static Future<?> runAsync(ExceptionalHandler callable) {
        return submit(COMMON_POOL_NAME, callable);
    }

    public static Future<?> runAsync(ExceptionalHandler callable, FinalConsumer<Exception> exceptionHandler) {
        return submit(COMMON_POOL_NAME, callable, exceptionHandler);
    }

    public static <V> Future<V> runAsync(Callable<V> callable) {
        return submit(COMMON_POOL_NAME, callable);
    }

    public static <V> Future<V> submit(String name, Callable<V> callable) {
        ThreadPoolInfo info = THREAD_POOL_INFO_MAP.get(name);
        final long task = info.submitTask();
        return Assert.notNull(POOL_EXECUTOR_MAP.get(name), "指定的线程池不存在: " + name).submit(() -> {
            info.runTask(task);
            V res;
            try {
                res = callable.call();
            } catch (Exception e) {
                info.completeTask(task, false);
                logger.error("任务执行异常[" + Thread.currentThread().getName() + "]", e);
                throw new SimplifiedException("任务执行异常[" + Thread.currentThread().getName() + "]", e);
            }
            info.completeTask(task, true);
            return res;
        });
    }

    public static Future<?> submit(String name, ExceptionalHandler callable) {
        ThreadPoolInfo info = THREAD_POOL_INFO_MAP.get(name);
        final long task = info.submitTask();
        return Assert.notNull(POOL_EXECUTOR_MAP.get(name), "指定的线程池不存在: " + name).submit(() -> {
            info.runTask(task);
            try {
                callable.handle();
            } catch (Exception e) {
                info.completeTask(task, false);
                logger.error("任务执行异常[" + Thread.currentThread().getName() + "]", e);
                return;
            }
            info.completeTask(task, true);
        });
    }

    public static Future<?> submit(String name, ExceptionalHandler callable, FinalConsumer<Exception> exceptionHandler) {
        ThreadPoolInfo info = THREAD_POOL_INFO_MAP.get(name);
        final long task = info.submitTask();
        return Assert.notNull(POOL_EXECUTOR_MAP.get(name), "指定的线程池不存在: " + name).submit(() -> {
            info.runTask(task);
            try {
                callable.handle();
            } catch (Exception e) {
                info.completeTask(task, false);
                exceptionHandler.accept(e);
                return;
            }
            info.completeTask(task, true);
        });
    }

    public static void shutdown(String name) {
        Assert.notTrue(COMMON_POOL_NAME.equals(name), "公共线程池不允许用户关闭");
        Assert.notNull(POOL_EXECUTOR_MAP.remove(name), "指定的线程池不存在: " + name).shutdown();
        THREAD_POOL_INFO_MAP.remove(name);
    }

    public static List<Runnable> shutdownNow(String name) {
        Assert.notTrue(COMMON_POOL_NAME.equals(name), "公共线程池不允许用户关闭");
        THREAD_POOL_INFO_MAP.remove(name);
        return Assert.notNull(POOL_EXECUTOR_MAP.remove(name), "指定的线程池不存在: " + name).shutdownNow();
    }

    public static List<ThreadPoolInfo> statistic() {
        return new ArrayList<>(THREAD_POOL_INFO_MAP.values());
    }

    private static ThreadFactory buildFactory(String name, Boolean daemon, Integer priority, Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        final AtomicLong count = new AtomicLong(0);
        final String namePattern = "ThreadPool-%s-%d";
        return runnable -> {
            Thread thread = THREAD_FACTORY.newThread(runnable);
            thread.setName(String.format(Locale.ROOT, namePattern, name, count.getAndIncrement()));
            if (daemon != null) {
                thread.setDaemon(daemon);
            }
            if (priority != null) {
                thread.setPriority(priority);
            }
            if (uncaughtExceptionHandler != null) {
                thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            }
            return thread;
        };
    }

    public static class ThreadPoolInfo {
        private static final AtomicLong taskIdGenerator = new AtomicLong(0L);

        /**
         * 线程池名称
         */
        private String name;

        /**
         * 核心线程数
         */
        private int coreSize;

        /**
         * 最大线程数
         */
        private int maxSize;

        /**
         * 阻塞队列长度
         */
        private int queueCapacity;

        /**
         * 合计任务数
         */
        private AtomicLong taskCnt = new AtomicLong(0L);

        /**
         * 正在运行的任务数
         */
        private AtomicLong runningTaskCnt = new AtomicLong(0L);

        /**
         * 阻塞的任务数
         */
        private AtomicLong blockedTaskCnt = new AtomicLong(0L);

        /**
         * 合计已完成任务数
         */
        private AtomicLong completedTaskCnt = new AtomicLong(0L);

        /**
         * 合计正确完成的任务数
         */
        private AtomicLong correctCompletedCnt = new AtomicLong(0L);

        /**
         * 线程池所有任务累计执行时间
         */
        private LongAccumulator accrueExecTime = new LongAccumulator((x, y) -> x + y, 0L);

        /**
         * 线程池所有任务累计等待时间
         */
        private LongAccumulator accrueWaitTime = new LongAccumulator((x, y) -> x + y, 0L);

        /**
         * 线程池单个任务最大执行时间
         */
        private AtomicLong maxExecTime = new AtomicLong(0L);

        /**
         * 线程池单个任务最小执行时间
         */
        private AtomicLong minExecTime = new AtomicLong(Long.MAX_VALUE);

        /**
         * 线程池单个任务最大等待时间
         */
        private AtomicLong maxWaitTime = new AtomicLong(0L);

        /**
         * 线程池单个任务最小等待时间
         */
        private AtomicLong minWaitTime = new AtomicLong(Long.MAX_VALUE);

        /**
         * 未完成任务列表
         */
        private Map<Long, TaskInfo> tasks = new ConcurrentHashMap<>();

        private ThreadPoolInfo(String name, int coreSize, int maxSize, int queueCapacity) {
            this.name = name;
            this.coreSize = coreSize;
            this.maxSize = maxSize;
            this.queueCapacity = queueCapacity;
        }

        private long submitTask() {
            long id = taskIdGenerator.getAndIncrement();
            TaskInfo taskInfo = new TaskInfo();
            taskInfo.submitTime = System.currentTimeMillis();
            tasks.put(id, taskInfo);
            taskCnt.incrementAndGet();
            blockedTaskCnt.incrementAndGet();
            return id;
        }

        private void runTask(long taskId) {
            TaskInfo taskInfo = tasks.get(taskId);
            if (null != taskInfo) {
                taskInfo.runTime = System.currentTimeMillis();
                blockedTaskCnt.decrementAndGet();
                runningTaskCnt.incrementAndGet();
                long waitTime = taskInfo.runTime - taskInfo.submitTime;
                accrueWaitTime.accumulate(waitTime);
                maxWaitTime.accumulateAndGet(waitTime, Math::max);
                minWaitTime.accumulateAndGet(waitTime, Math::min);
            }
        }

        private void completeTask(long taskId, boolean succ) {
            TaskInfo taskInfo = tasks.remove(taskId);
            if (null != taskInfo) {
                taskInfo.finishTime = System.currentTimeMillis();
                runningTaskCnt.decrementAndGet();
                completedTaskCnt.incrementAndGet();
                if (succ) {
                    correctCompletedCnt.incrementAndGet();
                }
                long execTime = taskInfo.finishTime - taskInfo.runTime;
                accrueExecTime.accumulate(execTime);
                maxExecTime.accumulateAndGet(execTime, Math::max);
                minExecTime.accumulateAndGet(execTime, Math::min);
            }
        }

        public String getName() {
            return name;
        }

        public int getCoreSize() {
            return coreSize;
        }

        public int getMaxSize() {
            return maxSize;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public long getTaskCnt() {
            return taskCnt.get();
        }

        public long getRunningTaskCnt() {
            return runningTaskCnt.get();
        }

        public long getBlockedTaskCnt() {
            return blockedTaskCnt.get();
        }

        public long getCompletedTaskCnt() {
            return completedTaskCnt.get();
        }

        public long getCorrectCompletedCnt() {
            return correctCompletedCnt.get();
        }

        public long getAccrueExecTime() {
            return accrueExecTime.get();
        }

        public long getAccrueWaitTime() {
            return accrueWaitTime.get();
        }

        public long getMaxExecTime() {
            return maxExecTime.get();
        }

        public long getMinExecTime() {
            return minExecTime.get();
        }

        public long getMaxWaitTime() {
            return maxWaitTime.get();
        }

        public long getMinWaitTime() {
            return minWaitTime.get();
        }

        @Override
        public String toString() {
            return "ThreadPoolInfo{" +
                ", name='" + name + '\'' +
                ", coreSize=" + coreSize +
                ", maxSize=" + maxSize +
                ", queueCapacity=" + queueCapacity +
                ", taskCnt=" + taskCnt +
                ", runningTaskCnt=" + runningTaskCnt +
                ", blockedTaskCnt=" + blockedTaskCnt +
                ", completedTaskCnt=" + completedTaskCnt +
                ", correctCompletedCnt=" + correctCompletedCnt +
                ", accrueExecTime=" + accrueExecTime +
                ", accrueWaitTime=" + accrueWaitTime +
                ", maxExecTime=" + maxExecTime +
                ", minExecTime=" + minExecTime +
                ", maxWaitTime=" + maxWaitTime +
                ", minWaitTime=" + minWaitTime +
                '}';
        }
    }

    public static class TaskInfo {
        private long submitTime;
        private long runTime;
        private long finishTime;
    }
}
