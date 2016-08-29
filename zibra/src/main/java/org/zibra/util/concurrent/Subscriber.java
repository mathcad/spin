package org.zibra.util.concurrent;

final class Subscriber<R, V> {
    public final Callback<R, V> onfulfill;
    public final Callback<R, Throwable> onreject;
    public final Promise<R> next;
    public Subscriber(Callback<R, V> onfulfill, Callback<R, Throwable> onreject, Promise<R> next) {
        this.onfulfill = onfulfill;
        this.onreject = onreject;
        this.next = next;
    }
}