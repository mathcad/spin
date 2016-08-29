package org.zibra.util.concurrent;

public interface AsyncFunc<R, V> extends Callback<R, V> {
    Promise<R> call(V value) throws Throwable;
}
