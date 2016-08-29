package org.zibra.common;

public interface HproseCallback<T> {
    void handler(T result, Object[] arguments);
}
