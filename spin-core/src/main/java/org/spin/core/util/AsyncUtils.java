package org.spin.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;
import org.spin.core.function.ExceptionalHandler;
import org.spin.core.function.FinalConsumer;
import org.spin.core.throwable.SimplifiedException;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.stream.Collectors;

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

    private static final Map<String, ThreadPoolWrapper> POOL_EXECUTOR_MAP = new ConcurrentHashMap<>();

    static {
        POOL_EXECUTOR_MAP.put(COMMON_POOL_NAME, new ThreadPoolWrapper(COMMON_POOL_NAME, 10, 200, 30L,
            TimeUnit.SECONDS,
            5,
            new ThreadPoolExecutor.CallerRunsPolicy()));
    }

    /**
     * 初始化一个指定名称的线程池
     *
     * @param name     线程池名称
     * @param poolSize 最大线程数
     */
    public static void initThreadPool(String name, int poolSize) {
        initThreadPool(name, poolSize, poolSize, 10000L, -1, new ThreadPoolExecutor.AbortPolicy());
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
        initThreadPool(name, corePoolSize, maxPoolSize, 10000L, queueSize, new ThreadPoolExecutor.AbortPolicy());
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
            ThreadPoolWrapper poolWrapper = POOL_EXECUTOR_MAP.remove(name);
            poolWrapper.executor.shutdown();
        }

        ThreadPoolWrapper poolWrapper = new ThreadPoolWrapper(name, corePoolSize, maxPoolSize, keepAliveTimeInMs,
            TimeUnit.MILLISECONDS,
            queueSize,
            rejectedExecutionHandler);

        POOL_EXECUTOR_MAP.put(name, poolWrapper);
    }

    /**
     * 提交任务到公用线程池
     *
     * @param callable 任务
     * @param <V>      返回结果类型
     * @return Future结果
     */
    public static <V> Future<V> runAsync(Callable<V> callable) {
        return submit(COMMON_POOL_NAME, callable);
    }

    /**
     * 提交任务到公用线程池
     *
     * @param callable 任务
     * @return Future结果
     */
    public static Future<?> runAsync(ExceptionalHandler callable) {
        return submit(COMMON_POOL_NAME, callable);
    }

    /**
     * 提交任务到公用线程池
     *
     * @param callable         任务
     * @param exceptionHandler 异常处理逻辑
     * @return Future结果
     */
    public static Future<?> runAsync(ExceptionalHandler callable, FinalConsumer<Exception> exceptionHandler) {
        return submit(COMMON_POOL_NAME, callable, exceptionHandler);
    }

    /**
     * 提交任务到指定线程池
     *
     * @param name     线程池名称
     * @param callable 任务
     * @param <V>      返回结果类型
     * @return Future结果
     */
    public static <V> Future<V> submit(String name, Callable<V> callable) {
        ThreadPoolWrapper poolWrapper = Assert.notNull(POOL_EXECUTOR_MAP.get(name), "指定的线程池不存在: " + name);
        final long task = poolWrapper.info.submitTask();
        return poolWrapper.executor.submit(() -> {
            poolWrapper.info.runTask(task);
            V res;
            try {
                res = callable.call();
            } catch (Exception e) {
                poolWrapper.info.completeTask(task, false);
                logger.error("任务执行异常[" + Thread.currentThread().getName() + "]", e);
                throw new SimplifiedException("任务执行异常[" + Thread.currentThread().getName() + "]", e);
            }
            poolWrapper.info.completeTask(task, true);
            return res;
        });
    }

    /**
     * 提交任务到指定线程池
     *
     * @param name     线程池名称
     * @param callable 任务
     * @return Future结果
     */
    public static Future<?> submit(String name, ExceptionalHandler callable) {
        ThreadPoolWrapper poolWrapper = Assert.notNull(POOL_EXECUTOR_MAP.get(name), "指定的线程池不存在: " + name);
        final long task = poolWrapper.info.submitTask();
        return poolWrapper.executor.submit(() -> {
            poolWrapper.info.runTask(task);
            try {
                callable.handle();
            } catch (Exception e) {
                poolWrapper.info.completeTask(task, false);
                logger.error("任务执行异常[" + Thread.currentThread().getName() + "]", e);
                return;
            }
            poolWrapper.info.completeTask(task, true);
        });
    }

    /**
     * 提交任务到指定线程池
     *
     * @param name             线程池名称
     * @param callable         任务
     * @param exceptionHandler 异常处理逻辑
     * @return Future结果
     */
    public static Future<?> submit(String name, ExceptionalHandler callable, FinalConsumer<Exception> exceptionHandler) {
        ThreadPoolWrapper poolWrapper = Assert.notNull(POOL_EXECUTOR_MAP.get(name), "指定的线程池不存在: " + name);
        final long task = poolWrapper.info.submitTask();
        return poolWrapper.executor.submit(() -> {
            poolWrapper.info.runTask(task);
            try {
                callable.handle();
            } catch (Exception e) {
                poolWrapper.info.completeTask(task, false);
                exceptionHandler.accept(e);
                return;
            }
            poolWrapper.info.completeTask(task, true);
        });
    }

    /**
     * 关闭指定线程池
     *
     * @param name 线程池名称
     */
    public static void shutdown(String name) {
        Assert.notTrue(COMMON_POOL_NAME.equals(name), "公共线程池不允许用户关闭");
        Assert.notNull(POOL_EXECUTOR_MAP.remove(name), "指定的线程池不存在: " + name).executor.shutdown();
    }

    /**
     * 立刻关闭指定线程池
     *
     * @param name 线程池名称
     * @return 未提交运行的任务列表
     */
    public static List<Runnable> shutdownNow(String name) {
        Assert.notTrue(COMMON_POOL_NAME.equals(name), "公共线程池不允许用户关闭");
        return Assert.notNull(POOL_EXECUTOR_MAP.remove(name), "指定的线程池不存在: " + name).executor.shutdownNow();
    }

    /**
     * 获取线程池统计信息
     *
     * @return 统计信息
     */
    public static List<ThreadPoolInfo> statistic() {
        return POOL_EXECUTOR_MAP.values().stream().map(ThreadPoolWrapper::getInfo).collect(Collectors.toList());
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

    public static class ThreadPoolWrapper {
        private final ThreadPoolExecutor executor;
        private final ThreadPoolInfo info;

        /**
         * 线程池构造方法
         *
         * @param name                     线程池名称
         * @param corePoolSize             核心线程数
         * @param maxPoolSize              最大线程数
         * @param keepAliveTimeInMs        空闲线程存活时间(毫秒)
         * @param queueSize                阻塞队列长度，负值表示无界
         * @param rejectedExecutionHandler 拒绝策略
         */
        public ThreadPoolWrapper(String name, int corePoolSize, int maxPoolSize, long keepAliveTimeInMs, TimeUnit timeUnit, int queueSize, RejectedExecutionHandler rejectedExecutionHandler) {
            int qs = queueSize >= 0 ? queueSize : Integer.MAX_VALUE;
            executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTimeInMs,
                timeUnit,
                qs == 0 ? new SynchronousQueue<>() : new LinkedBlockingQueue<>(qs),
                buildFactory(name, null, null, (thread, throwable) -> {
                }),
                rejectedExecutionHandler);
            info = new ThreadPoolInfo(name, corePoolSize, maxPoolSize, qs);
        }

        public ThreadPoolExecutor getExecutor() {
            return executor;
        }

        public ThreadPoolInfo getInfo() {
            return info;
        }
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
