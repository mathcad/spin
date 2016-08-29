package org.zibra.util.concurrent;

public interface AsyncCall<T> {
    Promise<T> call() throws Throwable;
}
