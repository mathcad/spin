package org.zibra.util.concurrent;

public interface Call<T> {
    T call() throws Throwable;
}
