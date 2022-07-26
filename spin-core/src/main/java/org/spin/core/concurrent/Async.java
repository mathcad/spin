package org.spin.core.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;
import org.spin.core.function.ExceptionalHandler;
import org.spin.core.function.FinalConsumer;
import org.spin.core.throwable.SpinException;
import org.spin.core.trait.Order;
import org.spin.core.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.stream.Collectors;

/**
 * 线程池工具
 * <p>提供全局的异步调用与线程池工具</p>
 * <p>Created by xuweinan on 2019/2/19</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public final class Async extends Util {
    private static final Logger logger = LoggerFactory.getLogger(Async.class);

    private static final String GLOBAL_INTERCEPTOR = "ALL";
    private static final String COMMON_POOL_NAME = "GlobalCommon";
    private static final ThreadFactory THREAD_FACTORY = Executors.defaultThreadFactory();

    private static final ConcurrentHashMap<String, ThreadPoolWrapper> POOL_EXECUTOR_MAP = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, List<AsyncInterceptor>> INTERCEPTORS = new ConcurrentHashMap<>();

    private static long poolTimeout = 1000L;

    static {
        POOL_EXECUTOR_MAP.put(COMMON_POOL_NAME, new ThreadPoolWrapper(COMMON_POOL_NAME, 10, 200, 30L,
            TimeUnit.SECONDS,
            5,
            new ThreadPoolExecutor.CallerRunsPolicy()));
        POOL_EXECUTOR_MAP.get(COMMON_POOL_NAME).init();
    }

    private Async() {
    }

    static void registerInterceptor(AsyncInterceptor interceptor) {
        String poolName = interceptor.getPoolName();
        List<AsyncInterceptor> list = INTERCEPTORS.get(poolName);
        if (null == list) {
            list = new ArrayList<>();
        } else {
            list = new ArrayList<>(list);
        }

        list.add(interceptor);
        list.sort(Comparator.comparingInt(Order::getOrder));
        INTERCEPTORS.put(poolName, list);
    }

    /**
     * 初始化一个指定名称的线程池
     *
     * @param name     线程池名称
     * @param poolSize 固定线程数
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

        Assert.gt(corePoolSize, 0, "核心线程数必须大于0");
        Assert.ge(maxPoolSize, maxPoolSize, "最大线程数不能小于核心线程数");
        Assert.gt(keepAliveTimeInMs, 0, "空闲线程存活时间必须大于0");
        ThreadPoolWrapper poolWrapper = new ThreadPoolWrapper(name, corePoolSize, maxPoolSize, keepAliveTimeInMs,
            TimeUnit.MILLISECONDS,
            queueSize,
            rejectedExecutionHandler);

        Assert.isTrue(null == POOL_EXECUTOR_MAP.putIfAbsent(name, poolWrapper), "线程池已经存在: " + name);
        POOL_EXECUTOR_MAP.get(name).init();
    }

    /**
     * 提交任务到公用线程池
     *
     * @param callable 任务
     * @param <V>      返回结果类型
     * @return Future结果
     */
    public static <V> Future<V> run(Callable<V> callable) {
        return submit(COMMON_POOL_NAME, callable);
    }

    /**
     * 提交任务到公用线程池
     *
     * @param callable 任务
     */
    public static void run(ExceptionalHandler<Exception> callable) {
        execute(COMMON_POOL_NAME, callable);
    }

    /**
     * 提交任务到公用线程池
     *
     * @param callable         任务
     * @param exceptionHandler 异常处理逻辑
     * @param <V>              返回结果类型
     * @return Future结果
     */
    public static <V> Future<V> run(Callable<V> callable, FinalConsumer<Exception> exceptionHandler) {
        return submit(COMMON_POOL_NAME, callable, exceptionHandler);
    }

    /**
     * 提交任务到公用线程池
     *
     * @param callable         任务
     * @param exceptionHandler 异常处理逻辑
     */
    public static void run(ExceptionalHandler<Exception> callable, FinalConsumer<Exception> exceptionHandler) {
        execute(COMMON_POOL_NAME, callable, exceptionHandler);
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
        return submit(name, callable, null);
    }


    /**
     * 提交任务到指定线程池
     *
     * @param name             线程池名称
     * @param callable         任务
     * @param exceptionHandler 异常处理逻辑
     * @param <V>              返回结果类型
     * @return Future结果
     */
    public static <V> Future<V> submit(String name, Callable<V> callable, FinalConsumer<Exception> exceptionHandler) {
        ThreadPoolWrapper poolWrapper = Assert.notNull(POOL_EXECUTOR_MAP.get(name), "指定的线程池不存在: " + name);
        checkReady(poolWrapper);
        final long task = poolWrapper.info.submitTask();
        // beforeAsync
        AsyncContext context = new AsyncContext();
        intercept(GLOBAL_INTERCEPTOR, context, 1);
        intercept(name, context, 1);
        Future<V> result = poolWrapper.executor.submit(() -> {
            logger.info("Task Scheduled from : {}", context.getScheduleThreadName());
            // beforeExecute
            intercept(GLOBAL_INTERCEPTOR, context, 2);
            intercept(name, context, 2);
            poolWrapper.info.runTask(task);
            try {
                V res = callable.call();
                poolWrapper.info.completeTask(task, true);
                return res;
            } catch (Exception e) {
                poolWrapper.info.completeTask(task, false);
                if (null != exceptionHandler) {
                    exceptionHandler.accept(e);
                }
                return null;
            } finally {
                // afterExecute
                intercept(GLOBAL_INTERCEPTOR, context, 3);
                intercept(name, context, 3);
            }
        });

        // afterAsync
        intercept(GLOBAL_INTERCEPTOR, context, 4);
        intercept(name, context, 4);
        return result;
    }

    /**
     * 提交任务到指定线程池
     *
     * @param name     线程池名称
     * @param callable 任务
     */
    public static void execute(String name, ExceptionalHandler<Exception> callable) {
        execute(name, callable, null);
    }

    /**
     * 提交任务到指定线程池
     *
     * @param name             线程池名称
     * @param callable         任务
     * @param exceptionHandler 异常处理逻辑
     */
    public static void execute(String name, ExceptionalHandler<Exception> callable, FinalConsumer<Exception> exceptionHandler) {
        ThreadPoolWrapper poolWrapper = Assert.notNull(POOL_EXECUTOR_MAP.get(name), "指定的线程池不存在: " + name);
        checkReady(poolWrapper);
        final long task = poolWrapper.info.submitTask();
        // beforeAsync
        AsyncContext context = new AsyncContext();
        intercept(GLOBAL_INTERCEPTOR, context, 1);
        intercept(name, context, 1);
        poolWrapper.executor.execute(() -> {
            logger.info("Task Scheduled from : {}", context.getScheduleThreadName());
            // beforeExecute
            intercept(GLOBAL_INTERCEPTOR, context, 2);
            intercept(name, context, 2);
            poolWrapper.info.runTask(task);
            try {
                callable.handle();
            } catch (Exception e) {
                poolWrapper.info.completeTask(task, false);
                if (null != exceptionHandler) {
                    exceptionHandler.accept(e);
                }
                return;
            } finally {
                // afterExecute
                intercept(GLOBAL_INTERCEPTOR, context, 3);
                intercept(name, context, 3);
            }
            poolWrapper.info.completeTask(task, true);
        });
        // afterAsync
        intercept(GLOBAL_INTERCEPTOR, context, 4);
        intercept(name, context, 4);
    }

    /**
     * 关闭指定线程池
     *
     * @param name 线程池名称
     */
    public static void shutdown(String name) {
        Assert.notTrue(COMMON_POOL_NAME.equals(name), "公共线程池不允许用户关闭");
        Assert.notNull(POOL_EXECUTOR_MAP.remove(name), "指定的线程池不存在: " + name).shutdown();
    }

    /**
     * 立刻关闭指定线程池
     *
     * @param name 线程池名称
     * @return 未提交运行的任务列表
     */
    public static List<Runnable> shutdownNow(String name) {
        Assert.notTrue(COMMON_POOL_NAME.equals(name), "公共线程池不允许用户关闭");
        return Assert.notNull(POOL_EXECUTOR_MAP.remove(name), "指定的线程池不存在: " + name).shutdownNow();
    }

    /**
     * 获取线程池统计信息
     *
     * @return 统计信息
     */
    public static List<ThreadPoolInfo> statistic() {
        return POOL_EXECUTOR_MAP.values().stream().map(ThreadPoolWrapper::getInfo).collect(Collectors.toList());
    }

    /**
     * 将所有线程池统计信息输出到日志
     */
    public static void logPoolStatisticInfo() {
        POOL_EXECUTOR_MAP.values().forEach(it -> logger.info("线程池[{}]当前状态: {}", it.name, it.getInfo().infoStr()));
    }

    /**
     * 设置线程池就绪等待时间
     *
     * @param poolTimeout 等待时间
     */
    public static void setPoolTimeout(long poolTimeout) {
        Async.poolTimeout = poolTimeout;
    }

    public static ThreadFactory buildFactory(String name, Boolean daemon, Integer priority, Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
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

    private static void checkReady(ThreadPoolWrapper poolWrapper) {
        long wait = System.currentTimeMillis();
        while (poolWrapper.status != ThreadPoolState.READY) {
            long w = System.currentTimeMillis() - wait;
            if (w > poolTimeout) {
                throw new SpinException("动作超时，线程池尚未就绪");
            }
            Thread.yield();
        }
    }

    private static void intercept(String poolName, AsyncContext context, int step) {
        if (INTERCEPTORS.containsKey(poolName)) {
            List<AsyncInterceptor> interceptors = INTERCEPTORS.get(poolName);
            for (AsyncInterceptor interceptor : interceptors) {
                try {
                    switch (step) {
                        case 1:
                            interceptor.preAsync(context);
                            break;
                        case 2:
                            interceptor.onReady(context);
                            break;
                        case 3:
                            interceptor.onFinish(context);
                            break;
                        case 4:
                            interceptor.afterAsync(context);
                            break;
                    }
                } catch (Exception e) {
                    logger.error("AsyncInterceptor执行异常[" + poolName + "]: ", e);
                }
            }
        }
    }

    public static class ThreadPoolWrapper {
        private ThreadPoolExecutor executor;
        private ThreadPoolInfo info;
        private volatile ThreadPoolState status = ThreadPoolState.NEW;

        private final String name;
        private final int corePoolSize;
        private final int maxPoolSize;
        private final long keepAliveTimeInMs;
        private final TimeUnit timeUnit;
        private final int queueSize;
        private final RejectedExecutionHandler rejectedExecutionHandler;

        private final Object lock = new Object();

        /**
         * 线程池构造方法
         *
         * @param name                     线程池名称
         * @param corePoolSize             核心线程数
         * @param maxPoolSize              最大线程数
         * @param keepAliveTimeInMs        空闲线程存活时间(毫秒)
         * @param timeUnit                 时间单位
         * @param queueSize                阻塞队列长度，负值表示无界
         * @param rejectedExecutionHandler 拒绝策略
         */
        public ThreadPoolWrapper(String name, int corePoolSize, int maxPoolSize, long keepAliveTimeInMs, TimeUnit timeUnit, int queueSize, RejectedExecutionHandler rejectedExecutionHandler) {
            int qs = queueSize >= 0 ? queueSize : Integer.MAX_VALUE;

            this.name = name;
            this.corePoolSize = corePoolSize;
            this.maxPoolSize = maxPoolSize;
            this.keepAliveTimeInMs = keepAliveTimeInMs;
            this.timeUnit = timeUnit;
            this.queueSize = qs;
            this.rejectedExecutionHandler = rejectedExecutionHandler;


        }

        public ThreadPoolExecutor getExecutor() {
            return executor;
        }

        public ThreadPoolInfo getInfo() {
            return info;
        }

        private void init() {
            if (ThreadPoolState.NEW == status) {
                synchronized (lock) {
                    if (ThreadPoolState.NEW == status) {
                        status = ThreadPoolState.PREPARING;
                        logger.info("Initiating Thread Pool[{}]: CoreSize - {}, MaxSize - {}, QueueSize - {}", name, corePoolSize, maxPoolSize, queueSize);
                        this.executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTimeInMs,
                            timeUnit,
                            queueSize == 0 ? new SynchronousQueue<>() : new LinkedBlockingQueue<>(queueSize),
                            buildFactory(name, COMMON_POOL_NAME.equals(name) ? true : null, null, (thread, throwable) -> {
                            }),
                            rejectedExecutionHandler);
                        this.info = new ThreadPoolInfo(name, corePoolSize, maxPoolSize, queueSize);
                        status = ThreadPoolState.READY;
                    }
                }
            }
        }

        private void shutdown() {
            if (null == executor || executor.isShutdown()) {
                return;
            }
            if (ThreadPoolState.READY == status) {
                synchronized (lock) {
                    if (ThreadPoolState.READY == status) {
                        status = ThreadPoolState.STOPPING;
                        logger.info("Terminating Thread Pool[{}]: CoreSize - {}, MaxSize - {}, QueueSize - {}", name, corePoolSize, maxPoolSize, queueSize);
                        executor.shutdown();
                    }
                }
            }
        }

        private List<Runnable> shutdownNow() {
            if (null == executor || executor.isShutdown()) {
                return Collections.emptyList();
            }
            if (ThreadPoolState.READY == status) {
                synchronized (lock) {
                    if (ThreadPoolState.READY == status) {
                        status = ThreadPoolState.STOPPING;
                        logger.info("Terminating Thread Pool[{}] Right Now: CoreSize - {}, MaxSize - {}, QueueSize - {}", name, corePoolSize, maxPoolSize, queueSize);
                        return executor.shutdownNow();
                    }
                }
            }
            return Collections.emptyList();
        }
    }

    public enum ThreadPoolState {
        /**
         * 新建
         */
        NEW,

        /**
         * 正在初始化
         */
        PREPARING,

        /**
         * 就绪
         */
        READY,

        /**
         * 正在停止
         */
        STOPPING
    }

    public static class ThreadPoolInfo {
        private static final AtomicLong taskIdGenerator = new AtomicLong(0L);

        /**
         * 线程池名称
         */
        private final String name;

        /**
         * 核心线程数
         */
        private final int coreSize;

        /**
         * 最大线程数
         */
        private final int maxSize;

        /**
         * 阻塞队列长度
         */
        private final int queueCapacity;

        /**
         * 合计任务数
         */
        private final AtomicLong taskCnt = new AtomicLong(0L);

        /**
         * 正在运行的任务数
         */
        private final AtomicLong runningTaskCnt = new AtomicLong(0L);

        /**
         * 阻塞的任务数
         */
        private final AtomicLong blockedTaskCnt = new AtomicLong(0L);

        /**
         * 合计已完成任务数
         */
        private final AtomicLong completedTaskCnt = new AtomicLong(0L);

        /**
         * 合计正确完成的任务数
         */
        private final AtomicLong correctCompletedCnt = new AtomicLong(0L);

        /**
         * 线程池所有任务累计执行时间
         */
        private final LongAccumulator accrueExecTime = new LongAccumulator(Long::sum, 0L);

        /**
         * 线程池所有任务累计等待时间
         */
        private final LongAccumulator accrueWaitTime = new LongAccumulator(Long::sum, 0L);

        /**
         * 线程池单个任务最大执行时间
         */
        private final AtomicLong maxExecTime = new AtomicLong(0L);

        /**
         * 线程池单个任务最小执行时间
         */
        private final AtomicLong minExecTime = new AtomicLong(Long.MAX_VALUE);

        /**
         * 线程池单个任务最大等待时间
         */
        private final AtomicLong maxWaitTime = new AtomicLong(0L);

        /**
         * 线程池单个任务最小等待时间
         */
        private final AtomicLong minWaitTime = new AtomicLong(Long.MAX_VALUE);

        /**
         * 未完成任务列表
         */
        private final Map<Long, TaskInfo> tasks = new ConcurrentHashMap<>();

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

        private void completeTask(long taskId, boolean success) {
            TaskInfo taskInfo = tasks.remove(taskId);
            if (null != taskInfo) {
                taskInfo.finishTime = System.currentTimeMillis();
                runningTaskCnt.decrementAndGet();
                completedTaskCnt.incrementAndGet();
                if (success) {
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

        public String infoStr() {
            return " 核心线程数=" + coreSize +
                " 最大线程数=" + maxSize +
                " 阻塞队列长度=" + queueCapacity +
                " 合计任务数=" + taskCnt +
                " 正在运行任务数=" + runningTaskCnt +
                " 阻塞任务数=" + blockedTaskCnt +
                " 合计已完成任务数=" + completedTaskCnt +
                " 合计正确完成任务数=" + correctCompletedCnt +
                " 累计执行时间=" + accrueExecTime +
                " 累计等待时间=" + accrueWaitTime +
                " 任务最长执行时间=" + maxExecTime +
                " 任务最短执行时间=" + minExecTime +
                " 任务最长等待时间=" + maxWaitTime +
                " 任务最短等待时间=" + minWaitTime;
        }
    }

    public static class TaskInfo {
        private long submitTime;
        private long runTime;
        private long finishTime;
    }
}
