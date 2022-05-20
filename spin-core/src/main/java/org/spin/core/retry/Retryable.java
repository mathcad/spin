package org.spin.core.retry;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/12/21</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@FunctionalInterface
public interface Retryable<T> {
    T apply(Exception exception) throws Exception;
}
