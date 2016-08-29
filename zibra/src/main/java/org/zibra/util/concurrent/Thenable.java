package org.zibra.util.concurrent;

public interface Thenable<V> {
    Promise<?> then(Action<V> onfulfill, Action<Throwable> onreject);

    <R> Promise<R> then(Func<R, V> onfulfill, Func<R, Throwable> onreject);

    <R> Promise<R> then(AsyncFunc<R, V> onfulfill, Func<R, Throwable> onreject);

    <R> Promise<R> then(AsyncFunc<R, V> onfulfill, AsyncFunc<R, Throwable> onreject);

    <R> Promise<R> then(Func<R, V> onfulfill, AsyncFunc<R, Throwable> onreject);
}