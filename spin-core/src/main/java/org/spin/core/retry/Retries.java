package org.spin.core.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.concurrent.Uninterruptibles;
import org.spin.core.function.ExceptionalConsumer;
import org.spin.core.throwable.RetryException;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * 重试
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/7/2</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class Retries {
    private static final Logger logger = LoggerFactory.getLogger(Retries.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final int attempts;
    private int delayMultiplier = 1;
    private long minDelay = 500L;
    private long maxDelay = 500L;

    private Predicate<Exception> exceptionRetryPredicate = Objects::nonNull;
    private final List<Class<? extends Exception>> exceptions = new LinkedList<>();
    private final List<Class<? extends Exception>> ignoreExceptions = new LinkedList<>();


    public static Retries attempts(int attempts) {
        return new Retries(attempts);
    }

    private Retries(int attempts) {
        this.attempts = attempts;
    }

    /**
     * 指定重试延迟时间
     *
     * @param backoff 重试延迟时间
     * @return 重试配置
     */
    public Retries delay(Duration backoff) {
        minDelay = backoff.toMillis();
        maxDelay = backoff.toMillis();
        return this;
    }

    /**
     * 指定重试延迟时间
     *
     * @param backoffInMillis 重试延迟时间(毫秒)
     * @return 重试配置
     */
    public Retries delay(long backoffInMillis) {
        minDelay = backoffInMillis;
        maxDelay = backoffInMillis;
        return this;
    }

    /**
     * 指定重试延迟时间
     *
     * @param backoff  重试延迟时间
     * @param timeUnit 时间单位
     * @return 重试配置
     */
    public Retries delay(long backoff, TimeUnit timeUnit) {
        minDelay = timeUnit.toMillis(backoff);
        maxDelay = timeUnit.toMillis(backoff);
        return this;
    }

    /**
     * 指定重试延迟时间
     *
     * @param minDelay 重试延迟时间
     * @return 重试配置
     */
    public Retries minDelay(Duration minDelay) {
        this.minDelay = minDelay.toMillis();
        if (this.minDelay > this.maxDelay) {
            this.maxDelay = this.minDelay;
        }
        return this;
    }

    /**
     * 指定重试延迟时间
     *
     * @param minDelayInMillis 重试延迟时间(毫秒)
     * @return 重试配置
     */
    public Retries minDelay(long minDelayInMillis) {
        minDelay = minDelayInMillis;
        if (this.minDelay > this.maxDelay) {
            this.maxDelay = this.minDelay;
        }
        return this;
    }

    /**
     * 指定重试延迟时间
     *
     * @param minDelay 重试延迟时间
     * @param timeUnit 时间单位
     * @return 重试配置
     */
    public Retries minDelay(long minDelay, TimeUnit timeUnit) {
        maxDelay = timeUnit.toMillis(minDelay);
        if (this.minDelay > this.maxDelay) {
            this.maxDelay = this.minDelay;
        }
        return this;
    }

    /**
     * 指定重试延迟时间
     *
     * @param maxDelay 重试延迟时间
     * @return 重试配置
     */
    public Retries maxDelay(Duration maxDelay) {
        this.maxDelay = maxDelay.toMillis();
        if (this.maxDelay < this.minDelay) {
            this.minDelay = this.maxDelay;
        }
        return this;
    }

    /**
     * 指定重试延迟时间
     *
     * @param maxDelayInMillis 重试延迟时间(毫秒)
     * @return 重试配置
     */
    public Retries maxDelay(long maxDelayInMillis) {
        maxDelay = maxDelayInMillis;
        if (this.maxDelay < this.minDelay) {
            this.minDelay = this.maxDelay;
        }
        return this;
    }

    /**
     * 指定重试延迟时间
     *
     * @param maxDelay 重试延迟时间
     * @param timeUnit 时间单位
     * @return 重试配置
     */
    public Retries maxDelay(long maxDelay, TimeUnit timeUnit) {
        this.maxDelay = timeUnit.toMillis(maxDelay);
        if (this.maxDelay < this.minDelay) {
            this.minDelay = this.maxDelay;
        }
        return this;
    }

    /**
     * 延迟的倍数
     *
     * @param delayMultiplier 延迟的倍数, 最小为1
     * @return 重试配置
     */
    public Retries delayMultiplier(int delayMultiplier) {
        this.delayMultiplier = Math.max(1, delayMultiplier);
        return this;
    }


    /**
     * 指定基于异常的重试条件
     *
     * @param predicate 重试条件
     * @return 重试配置
     */
    public Retries onException(Predicate<Exception> predicate) {
        this.exceptionRetryPredicate = predicate;
        return this;
    }

    /**
     * 指定需要重试的异常类型
     *
     * @param classes 会触发重试的异常类型
     * @return 重试配置
     */
    @SafeVarargs
    public final Retries onExceptions(Class<? extends Exception>... classes) {
        Collections.addAll(exceptions, classes);
        return this;
    }

    /**
     * 指定忽略的异常类型
     *
     * @param classes 忽略的异常类型列表
     * @return 重试配置
     */
    @SafeVarargs
    public final Retries ignoreExceptions(Class<? extends Exception>... classes) {
        Collections.addAll(ignoreExceptions, classes);
        return this;
    }

    /**
     * 指定基于结果的重试条件
     *
     * @param resultRetryPredicate 结果重试条件
     * @param <T>                  结果类型
     * @return 重试配置
     */
    public <T> RetriesWithResult<T> onResult(Predicate<T> resultRetryPredicate) {
        return new RetriesWithResult<>(resultRetryPredicate);
    }

    /**
     * 执行指定的操作，并根据配置的重试规则进行最多 {@code maxAttempts} 次重试(不包括首次执行, 最多执行maxAttempts+1次)
     *
     * @param handler 操作
     */
    public void exec(ExceptionalConsumer<Exception, Exception> handler) {
        Exception lastException = null;
        int i = 0;
        while (i <= attempts) {
            try {
                handler.accept(lastException);
                return;
            } catch (Exception e) {
                lastException = e;

                if (!checkException(e)) {
                    logger.info("Retries[{}/{}] *** EXCEPTION IGNORED [{}] *** , finish.", i, attempts, e.getClass().getName());
                    break;
                } else {
                    logger.info("Retries[{}/{}] *** OCCURRED EXCEPTION[{}] *** , will retry later...", i, attempts, e.getClass().getName());
                }
            }

            ++i;
            Uninterruptibles.sleepUninterruptibly(getBackoff() * delayMultiplier, TimeUnit.MILLISECONDS);
        }

//        throw lastException;
        throw new RetryException("所有重试均已失败, " + "重试次数: " + (i - 1), lastException);
    }

    private boolean checkException(Exception e) {
        for (Class<? extends Exception> ignoreException : ignoreExceptions) {
            if (ignoreException.isAssignableFrom(e.getClass())) {
                return false;
            }
        }

        for (Class<? extends Exception> exception : exceptions) {
            if (exception.isAssignableFrom(e.getClass())) {
                return true;
            }
        }

        return null != exceptionRetryPredicate && exceptionRetryPredicate.test(e);
    }

    private long getBackoff() {
        return maxDelay == minDelay ? minDelay : (SECURE_RANDOM.nextInt((int) (maxDelay - minDelay)) + minDelay);
    }

    /**
     * 重试
     * <p>DESCRIPTION</p>
     * <p>Created by xuweinan on 2020/7/2</p>
     *
     * @param <T> 泛型参数类型
     * @author xuweinan
     * @version 1.0
     */
    public class RetriesWithResult<T> {
        private Predicate<T> resultRetryPredicate;

        private RetriesWithResult(Predicate<T> resultRetryPredicate) {
            this.resultRetryPredicate = resultRetryPredicate;
        }

        /**
         * 指定重试延迟时间
         *
         * @param backoff 重试延迟时间
         * @return 重试配置
         */
        public RetriesWithResult<T> delay(Duration backoff) {
            minDelay = backoff.toMillis();
            maxDelay = backoff.toMillis();
            return this;
        }

        /**
         * 指定重试延迟时间
         *
         * @param backoffInMillis 重试延迟时间(毫秒)
         * @return 重试配置
         */
        public RetriesWithResult<T> delay(long backoffInMillis) {
            minDelay = backoffInMillis;
            maxDelay = backoffInMillis;
            return this;
        }

        /**
         * 指定重试延迟时间
         *
         * @param backoff  重试延迟时间
         * @param timeUnit 时间单位
         * @return 重试配置
         */
        public RetriesWithResult<T> delay(long backoff, TimeUnit timeUnit) {
            minDelay = timeUnit.toMillis(backoff);
            maxDelay = timeUnit.toMillis(backoff);
            return this;
        }

        /**
         * 指定重试延迟时间
         *
         * @param minDelay 重试延迟时间
         * @return 重试配置
         */
        public RetriesWithResult<T> minDelay(Duration minDelay) {
            Retries.this.minDelay = minDelay.toMillis();
            if (Retries.this.minDelay > Retries.this.maxDelay) {
                Retries.this.maxDelay = Retries.this.minDelay;
            }
            return this;
        }

        /**
         * 指定重试延迟时间
         *
         * @param minDelayInMillis 重试延迟时间(毫秒)
         * @return 重试配置
         */
        public RetriesWithResult<T> minDelay(long minDelayInMillis) {
            minDelay = minDelayInMillis;
            if (Retries.this.minDelay > Retries.this.maxDelay) {
                Retries.this.maxDelay = Retries.this.minDelay;
            }
            return this;
        }

        /**
         * 指定重试延迟时间
         *
         * @param minDelay 重试延迟时间
         * @param timeUnit 时间单位
         * @return 重试配置
         */
        public RetriesWithResult<T> minDelay(long minDelay, TimeUnit timeUnit) {
            maxDelay = timeUnit.toMillis(minDelay);
            if (Retries.this.minDelay > Retries.this.maxDelay) {
                Retries.this.maxDelay = Retries.this.minDelay;
            }
            return this;
        }

        /**
         * 指定重试延迟时间
         *
         * @param maxDelay 重试延迟时间
         * @return 重试配置
         */
        public RetriesWithResult<T> maxDelay(Duration maxDelay) {
            Retries.this.maxDelay = maxDelay.toMillis();
            if (Retries.this.maxDelay < Retries.this.minDelay) {
                Retries.this.minDelay = Retries.this.maxDelay;
            }
            return this;
        }

        /**
         * 指定重试延迟时间
         *
         * @param maxDelayInMillis 重试延迟时间(毫秒)
         * @return 重试配置
         */
        public RetriesWithResult<T> maxDelay(long maxDelayInMillis) {
            maxDelay = maxDelayInMillis;
            if (Retries.this.maxDelay < Retries.this.minDelay) {
                Retries.this.minDelay = Retries.this.maxDelay;
            }
            return this;
        }

        /**
         * 指定重试延迟时间
         *
         * @param maxDelay 重试延迟时间
         * @param timeUnit 时间单位
         * @return 重试配置
         */
        public RetriesWithResult<T> maxDelay(long maxDelay, TimeUnit timeUnit) {
            Retries.this.maxDelay = timeUnit.toMillis(maxDelay);
            if (Retries.this.maxDelay < Retries.this.minDelay) {
                Retries.this.minDelay = Retries.this.maxDelay;
            }
            return this;
        }

        /**
         * 延迟的倍数
         *
         * @param delayMultiplier 延迟的倍数, 最小为1
         */
        public void delayMultiplier(int delayMultiplier) {
            Retries.this.delayMultiplier = Math.max(1, delayMultiplier);
        }

        /**
         * 指定基于异常的重试条件
         *
         * @param predicate 重试条件
         * @return 重试配置
         */
        public RetriesWithResult<T> onException(Predicate<Exception> predicate) {
            Retries.this.exceptionRetryPredicate = predicate;
            return this;
        }

        /**
         * 指定需要重试的异常类型
         *
         * @param classes 会触发重试的异常类型
         * @return 重试配置
         */
        @SafeVarargs
        public final RetriesWithResult<T> onExceptions(Class<? extends Exception>... classes) {
            Collections.addAll(exceptions, classes);
            return this;
        }

        /**
         * 指定忽略的异常类型
         *
         * @param classes 忽略的异常类型列表
         * @return 重试配置
         */
        @SafeVarargs
        public final RetriesWithResult<T> ignoreExceptions(Class<? extends Exception>... classes) {
            Collections.addAll(ignoreExceptions, classes);
            return this;
        }

        /**
         * 执行指定的操作，并根据配置的重试规则进行最多 {@code maxAttempts} 次重试
         *
         * @param callable 操作
         * @return 操作结果
         */
        public T exec(Retryable<T> callable) {
            Exception lastException = null;
            int i = 0;
            while (i <= attempts) {
                try {
                    T result = callable.apply(lastException);
                    if (null == resultRetryPredicate || !resultRetryPredicate.test(result)) {
                        return result;
                    } else {
                        logger.info("Retries[{}/{}] *** RESULT NOT MATCH *** , will retry later...", i, attempts);
                    }
                } catch (Exception e) {
                    lastException = e;

                    if (!checkException(e)) {
                        logger.info("Retries[{}/{}] *** EXCEPTION IGNORED [{}] *** , finish.", i, attempts, e.getClass().getName());
                        break;
                    } else {
                        logger.info("Retries[{}/{}] *** OCCURRED EXCEPTION[{}] *** , will retry later...", i, attempts, e.getClass().getName());
                    }

                }

                Uninterruptibles.sleepUninterruptibly(getBackoff(), TimeUnit.MILLISECONDS);
                ++i;
            }

            throw new RetryException("所有重试均已失败, " + "重试次数: " + (i - 1), lastException);
        }
    }
}
