package org.spin.core.retry;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * 重试配置
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/7/2</p>
 *
 * @param <T> 泛型参数类型
 * @author xuweinan
 * @version 1.0
 */
public class RetryConfig<T> {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private long minWaitDuration = 500L;
    private long maxWaitDuration = 500L;

    private Predicate<Exception> exceptionRetryPredicate = Objects::nonNull;
    private Predicate<T> resultRetryPredicate;
    private final List<Class<? extends Exception>> exceptions = new LinkedList<>();
    private final List<Class<? extends Exception>> ignoreExceptions = new LinkedList<>();

    public static <T> RetryConfig<T> custom() {
        return new RetryConfig<>();
    }

    /**
     * 指定重试等待时间
     *
     * @param waitDuration 等待时间
     * @return 重试配置
     */
    public RetryConfig<T> waitDuration(Duration waitDuration) {
        minWaitDuration = waitDuration.toMillis();
        maxWaitDuration = waitDuration.toMillis();
        return this;
    }

    /**
     * 指定重试等待时间
     *
     * @param waitDurationInMillis 等待时间(毫秒)
     * @return 重试配置
     */
    public RetryConfig<T> waitDuration(long waitDurationInMillis) {
        minWaitDuration = waitDurationInMillis;
        maxWaitDuration = waitDurationInMillis;
        return this;
    }

    /**
     * 指定重试等待时间
     *
     * @param waitDuration 等待时间
     * @param timeUnit     时间单位
     * @return 重试配置
     */
    public RetryConfig<T> waitDuration(long waitDuration, TimeUnit timeUnit) {
        minWaitDuration = timeUnit.toMillis(waitDuration);
        maxWaitDuration = timeUnit.toMillis(waitDuration);
        return this;
    }

    /**
     * 指定重试等待时间
     *
     * @param minWaitDuration 最短等待时间
     * @param maxWaitDuration 最长等待时间
     * @return 重试配置
     */
    public RetryConfig<T> waitDuration(Duration minWaitDuration, Duration maxWaitDuration) {
        this.minWaitDuration = minWaitDuration.toMillis();
        this.maxWaitDuration = maxWaitDuration.toMillis();
        return this;
    }

    /**
     * 指定重试等待时间
     *
     * @param minWaitDurationInMillis 最短等待时间(毫秒)
     * @param maxWaitDurationInMillis 最长等待时间(毫秒)
     * @return 重试配置
     */
    public RetryConfig<T> waitDuration(long minWaitDurationInMillis, long maxWaitDurationInMillis) {
        minWaitDuration = minWaitDurationInMillis;
        maxWaitDuration = maxWaitDurationInMillis;
        return this;
    }

    /**
     * 指定重试等待时间
     *
     * @param minWaitDuration 最短等待时间
     * @param maxWaitDuration 最长等待时间
     * @param timeUnit        时间单位
     * @return 重试配置
     */
    public RetryConfig<T> waitDuration(long minWaitDuration, long maxWaitDuration, TimeUnit timeUnit) {
        this.minWaitDuration = timeUnit.toMillis(minWaitDuration);
        this.maxWaitDuration = timeUnit.toMillis(maxWaitDuration);
        return this;
    }

    /**
     * 指定基于结果的重试条件
     *
     * @param predicate 重试条件
     * @return 重试配置
     */
    public RetryConfig<T> retryOnResult(Predicate<T> predicate) {
        this.resultRetryPredicate = predicate;
        return this;
    }

    /**
     * 指定基于异常的重试条件
     *
     * @param predicate 重试条件
     * @return 重试配置
     */
    public RetryConfig<T> retryOnException(Predicate<Exception> predicate) {
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
    public final RetryConfig<T> retryExceptions(Class<? extends Exception>... classes) {
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
    public final RetryConfig<T> ignoreExceptions(Class<? extends Exception>... classes) {
        Collections.addAll(ignoreExceptions, classes);
        return this;
    }

    public boolean checkException(Exception e) {
        for (Class<? extends Exception> ignoreException : ignoreExceptions) {
            if (ignoreException.equals(e.getClass())) {
                return false;
            }
        }

        for (Class<? extends Exception> exception : exceptions) {
            if (exception.equals(e.getClass())) {
                return true;
            }
        }

        return null != exceptionRetryPredicate && exceptionRetryPredicate.test(e);
    }

    public boolean checkResult(T result) {
        return null != resultRetryPredicate && resultRetryPredicate.test(result);
    }

    public long getWaitDuration() {
        return maxWaitDuration == minWaitDuration ? minWaitDuration : (SECURE_RANDOM.nextInt((int) (maxWaitDuration - minWaitDuration)) + minWaitDuration);
    }

    public Predicate<Exception> getExceptionRetryPredicate() {
        return exceptionRetryPredicate;
    }

    public Predicate<T> getResultRetryPredicate() {
        return resultRetryPredicate;
    }
}
