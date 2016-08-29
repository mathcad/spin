package org.zibra.util.concurrent;

public interface Action<V> extends Callback<Void, V> {
    void call(V value) throws Throwable;
}
