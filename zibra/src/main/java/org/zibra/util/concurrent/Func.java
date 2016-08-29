package org.zibra.util.concurrent;

public interface Func<R, V> extends Callback<R, V> {
    R call(V value) throws Throwable;
}
