package org.spin.core.retry;

import org.spin.core.concurrent.Uninterruptibles;
import org.spin.core.function.ExceptionalConsumer;
import org.spin.core.throwable.SpinException;
import org.spin.core.util.Util;

import java.util.concurrent.TimeUnit;

/**
 * 重试工具
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/7/2</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public final class Retries extends Util {

    private Retries() {
    }

    private static final RetryConfig<Object> DEFAULT_RETRY_CONFIG = new RetryConfig<>();

    /**
     * 当指定操作发生异常时，以0.5s的间隔最多重试3次
     *
     * @param callable 操作
     * @param <T>      操作返回结果类型
     * @return 操作结果
     */
    public static <T> T retry(Retriable<T> callable) {
        return retry(callable, 3, DEFAULT_RETRY_CONFIG);
    }

    /**
     * 当指定的操作发生异常时，以0.5s的间隔最多重试 {@code maxAttempts} 次
     *
     * @param callable    操作
     * @param maxAttempts 最大重试次数
     * @param <T>         操作返回结果类型
     * @return 操作结果
     */
    public static <T> T retry(Retriable<T> callable, final int maxAttempts) {
        return retry(callable, maxAttempts, DEFAULT_RETRY_CONFIG);
    }

    /**
     * 执行指定的操作，并根据配置的重试规则进行最多 {@code maxAttempts} 次重试
     *
     * @param callable    操作
     * @param maxAttempts 最大重试次数
     * @param retryConfig 重试配置
     * @param <T>         操作返回结果类型
     * @return 操作结果
     */
    public static <T> T retry(Retriable<T> callable, final int maxAttempts, RetryConfig<Object> retryConfig) {
        if (null == retryConfig) {
            retryConfig = DEFAULT_RETRY_CONFIG;
        }
        Exception lastException = null;
        int i = 0;
        while (i <= maxAttempts) {
            try {
                T result = callable.apply(lastException);
                if (retryConfig.checkResult(result)) {
                    return result;
                }
            } catch (Exception e) {
                lastException = e;

                if (!retryConfig.checkException(e)) {
                    break;
                }

            }

            Uninterruptibles.sleepUninterruptibly(retryConfig.getWaitDuration(), TimeUnit.MILLISECONDS);
            ++i;
        }

        throw new SpinException("所有重试均已失败, " + "重试次数: " + i, lastException);
    }

    /**
     * 当指定操作发生异常时，以0.5s的间隔最多重试3次
     *
     * @param handler 操作
     */
    public static void retry(ExceptionalConsumer<Exception, Exception> handler) {
        retry(handler, 3, DEFAULT_RETRY_CONFIG);
    }

    /**
     * 当指定的操作发生异常时，以0.5s的间隔最多重试 {@code maxAttempts} 次
     *
     * @param handler     操作
     * @param maxAttempts 最大重试次数
     */
    public static void retry(ExceptionalConsumer<Exception, Exception> handler, final int maxAttempts) {
        retry(handler, maxAttempts, DEFAULT_RETRY_CONFIG);
    }

    /**
     * 执行指定的操作，并根据配置的重试规则进行最多 {@code maxAttempts} 次重试
     *
     * @param handler     操作
     * @param maxAttempts 最大重试次数
     * @param retryConfig 重试配置
     */
    public static void retry(ExceptionalConsumer<Exception, Exception> handler, final int maxAttempts, RetryConfig<Object> retryConfig) {
        if (null == retryConfig) {
            retryConfig = DEFAULT_RETRY_CONFIG;
        }
        Exception lastException = null;
        int i = 0;
        while (i <= maxAttempts) {
            try {
                handler.accept(lastException);
                return;
            } catch (Exception e) {
                lastException = e;

                if (!retryConfig.checkException(e)) {
                    break;
                }

            }

            Uninterruptibles.sleepUninterruptibly(retryConfig.getWaitDuration(), TimeUnit.MILLISECONDS);
            ++i;
        }

        throw new SpinException("所有重试均已失败, " + "重试次数: " + i, lastException);
    }

    @FunctionalInterface
    public interface Retriable<T> {
        T apply(Exception exception) throws Exception;
    }
}
