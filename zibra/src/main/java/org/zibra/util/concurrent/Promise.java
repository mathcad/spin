package org.zibra.util.concurrent;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class Promise<S> implements Resolver<S>, Rejector, Thenable<S> {
    private static ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();

    static {
        Threads.registerShutdownHandler(timer::shutdown);
    }

    private ConcurrentLinkedQueue<Subscriber<?, S>> subscribers = new ConcurrentLinkedQueue<>();
    private volatile AtomicReference<State> state = new AtomicReference<>(State.PENDING);
    private volatile S value;
    private volatile Throwable reason;

    public Promise() {
    }

    public Promise(Call<S> computation) {
        timer.execute(() -> {
            try {
                Promise.this.resolve(computation.call());
            } catch (Throwable e) {
                Promise.this.reject(e);
            }
        });
    }

    public Promise(AsyncCall<S> computation) {
        timer.execute(() -> {
            try {
                Promise.this.resolve(computation.call());
            } catch (Throwable e) {
                Promise.this.reject(e);
            }
        });
    }

    public Promise(Executor<S> executor) {
        executor.exec(this, this);
    }

    public static <T> Promise<T> value(T value) {
        Promise<T> promise = new Promise<>();
        promise.resolve(value);
        return promise;
    }

    public static <T> Promise<T> value(Promise<T> value) {
        Promise<T> promise = new Promise<>();
        promise.resolve(value);
        return promise;
    }

    public static <T> Promise<T> value(Thenable<T> value) {
        Promise<T> promise = new Promise<>();
        promise.resolve(value);
        return promise;
    }

    public static <T> Promise<T> error(Throwable reason) {
        Promise<T> promise = new Promise<>();
        promise.reject(reason);
        return promise;
    }

    public static <T> Promise<T> delayed(long duration, TimeUnit timeunit, Call<T> computation) {
        Promise<T> promise = new Promise<>();
        timer.schedule(() -> {
            try {
                promise.resolve(computation.call());
            } catch (Throwable e) {
                promise.reject(e);
            }
        }, duration, timeunit);
        return promise;
    }

    public static <T> Promise<T> delayed(long duration, TimeUnit timeunit, AsyncCall<T> computation) {
        Promise<T> promise = new Promise<>();
        timer.schedule(() -> {
            try {
                promise.resolve(computation.call());
            } catch (Throwable e) {
                promise.reject(e);
            }
        }, duration, timeunit);
        return promise;
    }

    public static <T> Promise<T> delayed(long duration, TimeUnit timeunit, T value) {
        Promise<T> promise = new Promise<>();
        timer.schedule(() -> promise.resolve(value), duration, timeunit);
        return promise;
    }

    public static <T> Promise<T> delayed(long duration, TimeUnit timeunit, Promise<T> value) {
        Promise<T> promise = new Promise<>();
        timer.schedule(() -> promise.resolve(value), duration, timeunit);
        return promise;
    }

    public static <T> Promise<T> delayed(long duration, Call<T> computation) {
        return delayed(duration, TimeUnit.MILLISECONDS, computation);
    }

    public static <T> Promise<T> delayed(long duration, AsyncCall<T> computation) {
        return delayed(duration, TimeUnit.MILLISECONDS, computation);
    }

    public static <T> Promise<T> delayed(long duration, T value) {
        return delayed(duration, TimeUnit.MILLISECONDS, value);
    }

    public static <T> Promise<T> delayed(long duration, Promise<T> value) {
        return delayed(duration, TimeUnit.MILLISECONDS, value);
    }

    public static <T> Promise<T> sync(Call<T> computation) {
        try {
            return value(computation.call());
        } catch (Throwable e) {
            return error(e);
        }
    }

    public static <T> Promise<T> sync(AsyncCall<T> computation) {
        try {
            return value(computation.call());
        } catch (Throwable e) {
            return error(e);
        }
    }

    public static boolean isThenable(Object value) {
        return value instanceof Thenable;
    }

    public static boolean isPromise(Object value) {
        return value instanceof Promise;
    }

    public static Promise<?> toPromise(Object value) {
        return isPromise(value) ? (Promise<?>) value : value(value);
    }

    @SuppressWarnings("unchecked")
    private static <T> void allHandler(Promise<T[]> promise, AtomicInteger count, T[] result, Object element, int i) {
        ((Promise<T>) toPromise(element)).then(
                value1 -> {
                    result[i] = value1;
                    if (count.decrementAndGet() == 0) {
                        promise.resolve(result);
                    }
                },
                promise::reject
        );
    }

    @SuppressWarnings("unchecked")
    public static <T> Promise<T[]> all(Object[] array, Class<T> type) {
        if (array == null) return value((T[]) null);
        int n = array.length;
        T[] result = (type == Object.class) ?
                (T[]) (new Object[n]) :
                (T[]) Array.newInstance(type, n);
        if (n == 0) return value(result);
        AtomicInteger count = new AtomicInteger(n);
        Promise<T[]> promise = new Promise<>();
        for (int i = 0; i < n; ++i) {
            allHandler(promise, count, result, array[i], i);
        }
        return promise;
    }

    public static Promise<Object[]> all(Object[] array) {
        return all(array, Object.class);
    }

    public static <T> Promise<T[]> all(Promise<Object[]> promise, Class<T> type) {
        return promise.then(new AsyncFunc<T[], Object[]>() {
            public Promise<T[]> call(Object[] array) throws Throwable {
                return all(array, type);
            }
        });
    }

    public static Promise<Object[]> all(Promise<Object[]> promise) {
        return all(promise, Object.class);
    }

    @SuppressWarnings("unchecked")
    public <T> Promise<T[]> all(Class<T> type) {
        return all((Promise<Object[]>) this, type);
    }

    @SuppressWarnings("unchecked")
    public Promise<Object[]> all() {
        return all((Promise<Object[]>) this);
    }

    public static Promise<Object[]> join(Object... args) {
        return all(args);
    }

    @SuppressWarnings("unchecked")
    public static <T> Promise<T> race(Object[] array, Class<T> type) {
        Promise<T> promise = new Promise<>();
        for (int i = 0, n = array.length; i < n; ++i) {
            ((Promise<T>) toPromise(array[i])).fill(promise);
        }
        return promise;
    }

    public static Promise<?> race(Object[] array) {
        return race(array, Object.class);
    }

    public static <T> Promise<T> race(Promise<Object[]> promise, Class<T> type) {
        return promise.then(new AsyncFunc<T, Object[]>() {
            public Promise<T> call(Object[] array) throws Throwable {
                return race(array, type);
            }
        });
    }

    public static Promise<?> race(Promise<Object[]> promise) {
        return race(promise, Object.class);
    }

    @SuppressWarnings("unchecked")
    public <T> Promise<T> race(Class<T> type) {
        return race((Promise<Object[]>) this, type);
    }

    @SuppressWarnings("unchecked")
    public Promise<?> race() {
        return race((Promise<Object[]>) this);
    }

    @SuppressWarnings("unchecked")
    public static <T> Promise<T> any(Object[] array, Class<T> type) {
        int n = array.length;
        if (n == 0) {
            return (Promise<T>) Promise.error(new IllegalArgumentException("any(): array must not be empty"));
        }
        RuntimeException reason = new RuntimeException("any(): all promises failed");
        Promise<T> promise = new Promise<>();
        AtomicInteger count = new AtomicInteger(n);
        for (int i = 0; i < n; ++i) {
            ((Promise<T>) toPromise(array[i])).then(
                    promise::resolve,
                    e -> {
                        if (count.decrementAndGet() == 0) {
                            promise.reject(reason);
                        }
                    }
            );
        }
        return promise;
    }

    public static Promise<?> any(Object[] array) {
        return any(array, Object.class);
    }

    public static <T> Promise<T> any(Promise<Object[]> promise, Class<T> type) {
        return promise.then(new AsyncFunc<T, Object[]>() {
            public Promise<T> call(Object[] array) throws Throwable {
                return any(array, type);
            }
        });
    }

    public static Promise<?> any(Promise<Object[]> promise) {
        return any(promise, Object.class);
    }

    @SuppressWarnings("unchecked")
    public <T> Promise<T> any(Class<T> type) {
        return any((Promise<Object[]>) this, type);
    }

    @SuppressWarnings("unchecked")
    public Promise<?> any() {
        return any((Promise<Object[]>) this);
    }

    public static Promise<?> run(Action<Object[]> handler, Object... args) {
        return all(args).then(handler);
    }

    public static <V> Promise<V> run(Func<V, Object[]> handler, Object... args) {
        return all(args).then(handler);
    }

    public static <V> Promise<V> run(AsyncFunc<V, Object[]> handler, Object... args) {
        return all(args).then(handler);
    }

    public static <T> Promise<?> run(Class<T> type, Action<T[]> handler, Object... args) {
        return all(args, type).then(handler);
    }

    public static <V, T> Promise<V> run(Class<T> type, Func<V, T[]> handler, Object... args) {
        return all(args, type).then(handler);
    }

    public static <V, T> Promise<V> run(Class<T> type, AsyncFunc<V, T[]> handler, Object... args) {
        return all(args, type).then(handler);
    }

    @SuppressWarnings("unchecked")
    public static <V> Promise<?> forEach(Action<V> callback, Object... args) {
        return all(args).then(array -> {
            if (array == null) return;
            for (int i = 0, n = array.length; i < n; ++i) {
                callback.call((V) array[i]);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <V> Action<Object[]> getForEachHandler(Handler<?, V> callback) {
        return array -> {
            if (array == null) return;
            for (int i = 0, n = array.length; i < n; ++i) {
                callback.call((V) array[i], i);
            }
        };
    }

    public static <V> Promise<?> forEach(Object[] array, Handler<?, V> callback) {
        return all(array).then(getForEachHandler(callback));
    }

    public static <V> Promise<?> forEach(Promise<Object[]> array, Handler<?, V> callback) {
        return all(array).then(getForEachHandler(callback));
    }

    public Promise<?> forEach(Handler<?, S> callback) {
        return this.all().then(getForEachHandler(callback));
    }

    @SuppressWarnings("unchecked")
    public static <V> Promise<Boolean> every(Func<Boolean, V> callback, Object... args) {
        return all(args).then(new Func<Boolean, Object[]>() {
            public Boolean call(Object[] array) throws Throwable {
                for (int i = 0, n = array.length; i < n; ++i) {
                    if (!callback.call((V) array[i])) return false;
                }
                return true;
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <V> Func<Boolean, Object[]> getEveryHandler(Handler<Boolean, V> callback) {
        return array -> {
            for (int i = 0, n = array.length; i < n; ++i) {
                if (!callback.call((V) array[i], i)) return false;
            }
            return true;
        };
    }

    public static <V> Promise<Boolean> every(Object[] array, Handler<Boolean, V> callback) {
        return all(array).then(getEveryHandler(callback));
    }

    public static <V> Promise<Boolean> every(Promise<Object[]> array, Handler<Boolean, V> callback) {
        return all(array).then(getEveryHandler(callback));
    }

    public <V> Promise<Boolean> every(Handler<Boolean, V> callback) {
        return all().then(getEveryHandler(callback));
    }

    @SuppressWarnings("unchecked")
    public static <V> Promise<Boolean> some(Func<Boolean, V> callback, Object... args) {
        return all(args).then(new Func<Boolean, Object[]>() {
            public Boolean call(Object[] array) throws Throwable {
                if (array == null) return false;
                for (int i = 0, n = array.length; i < n; ++i) {
                    if (callback.call((V) array[i])) return true;
                }
                return false;
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <V> Func<Boolean, Object[]> getSomeHandler(Handler<Boolean, V> callback) {
        return array -> {
            if (array == null) return false;
            for (int i = 0, n = array.length; i < n; ++i) {
                if (callback.call((V) array[i], i)) return true;
            }
            return false;
        };
    }

    public static <V> Promise<Boolean> some(Object[] array, Handler<Boolean, V> callback) {
        return all(array).then(getSomeHandler(callback));
    }

    public static <V> Promise<Boolean> some(Promise<Object[]> array, Handler<Boolean, V> callback) {
        return all(array).then(getSomeHandler(callback));
    }

    public <V> Promise<Boolean> some(Handler<Boolean, V> callback) {
        return this.all().then(getSomeHandler(callback));
    }

    @SuppressWarnings("unchecked")
    public static <V> Promise<Object[]> filter(Func<Boolean, V> callback, Object... args) {
        return all(args).then(new Func<Object[], Object[]>() {
            public Object[] call(Object[] array) throws Throwable {
                if (array == null) return null;
                int n = array.length;
                ArrayList<Object> result = new ArrayList<>(n);
                for (int i = 0; i < n; ++i) {
                    if (callback.call((V) array[i])) result.add(array[i]);
                }
                return result.toArray();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <V, T> Func<T[], T[]> getFilterHandler(Handler<Boolean, V> callback, Class<T> type) {
        return array -> {
            if (array == null) return null;
            int n = array.length;
            ArrayList<T> result = new ArrayList<>(n);
            for (int i = 0; i < n; ++i) {
                if (callback.call((V) array[i], i)) result.add(array[i]);
            }
            return result.toArray((type == Object.class) ?
                    (T[]) (new Object[result.size()]) :
                    (T[]) Array.newInstance(type, result.size()));
        };
    }


    public static <V, T> Promise<T[]> filter(Object[] array, Handler<Boolean, V> callback, Class<T> type) {
        return all(array, type).then(getFilterHandler(callback, type));
    }

    public static <V> Promise<Object[]> filter(Object[] array, Handler<Boolean, V> callback) {
        return filter(array, callback, Object.class);
    }

    public static <V, T> Promise<T[]> filter(Promise<Object[]> array, Handler<Boolean, V> callback, Class<T> type) {
        return all(array, type).then(getFilterHandler(callback, type));
    }

    public static <V> Promise<Object[]> filter(Promise<Object[]> array, Handler<Boolean, V> callback) {
        return filter(array, callback, Object.class);
    }

    public <V, T> Promise<T[]> filter(Handler<Boolean, V> callback, Class<T> type) {
        return all(type).then(getFilterHandler(callback, type));
    }

    public <V> Promise<Object[]> filter(Handler<Boolean, V> callback) {
        return filter(callback, Object.class);
    }

    @SuppressWarnings("unchecked")
    public static <V> Promise<Object[]> map(Func<?, V> callback, Object... args) {
        return all(args).then(new Func<Object[], Object[]>() {
            public Object[] call(Object[] array) throws Throwable {
                if (array == null) return null;
                int n = array.length;
                Object[] result = new Object[n];
                for (int i = 0; i < n; ++i) {
                    result[i] = callback.call((V) array[i]);
                }
                return result;
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <V, T> Func<T[], Object[]> getMapHandler(Handler<T, V> callback, Class<T> type) {
        return array -> {
            if (array == null) return null;
            int n = array.length;
            T[] result = (type == Object.class) ?
                    (T[]) (new Object[n]) :
                    (T[]) Array.newInstance(type, n);
            for (int i = 0; i < n; ++i) {
                result[i] = callback.call((V) array[i], i);
            }
            return result;
        };
    }

    @SuppressWarnings("unchecked")
    private static <V> Func<Object[], Object[]> getMapHandler(Handler<?, V> callback) {
        return array -> {
            if (array == null) return null;
            int n = array.length;
            Object[] result = new Object[n];
            for (int i = 0; i < n; ++i) {
                result[i] = callback.call((V) array[i], i);
            }
            return result;
        };
    }

    public static <V, T> Promise<T[]> map(Object[] array, Handler<T, V> callback, Class<T> type) {
        return all(array).then(getMapHandler(callback, type));
    }

    public static <V> Promise<Object[]> map(Object[] array, Handler<?, V> callback) {
        return all(array).then(getMapHandler(callback));
    }

    public static <V, T> Promise<T[]> map(Promise<Object[]> array, Handler<T, V> callback, Class<T> type) {
        return all(array).then(getMapHandler(callback, type));
    }

    public static <V> Promise<Object[]> map(Promise<Object[]> array, Handler<?, V> callback) {
        return all(array).then(getMapHandler(callback));
    }

    public <V, T> Promise<T[]> map(Handler<T, V> callback, Class<T> type) {
        return all().then(getMapHandler(callback, type));
    }

    public <V> Promise<Object[]> map(Handler<?, V> callback) {
        return all().then(getMapHandler(callback));
    }


    @SuppressWarnings("unchecked")
    private static <V> Func<V, Object[]> getReduceHandler(Reducer<V, V> callback) {
        return array -> {
            if (array == null) return null;
            int n = array.length;
            if (n == 0) return null;
            V result = (V) array[0];
            for (int i = 1; i < n; ++i) {
                result = callback.call(result, (V) array[i], i);
            }
            return result;
        };
    }

    public static <V> Promise<V> reduce(Object[] array, Reducer<V, V> callback) {
        return all(array).then(getReduceHandler(callback));
    }

    public static <V> Promise<V> reduce(Promise<Object[]> array, Reducer<V, V> callback) {
        return all(array).then(getReduceHandler(callback));
    }

    public <V> Promise<V> reduce(Reducer<V, V> callback) {
        return all().then(getReduceHandler(callback));
    }

    @SuppressWarnings("unchecked")
    private static <R, V> Func<R, Object[]> getReduceHandler(Reducer<R, V> callback, R initialValue) {
        return array -> {
            if (array == null) return initialValue;
            int n = array.length;
            if (n == 0) return initialValue;
            R result = initialValue;
            for (int i = 0; i < n; ++i) {
                result = callback.call(result, (V) array[i], i);
            }
            return result;
        };
    }

    public static <R, V> Promise<R> reduce(Object[] array, Reducer<R, V> callback, R initialValue) {
        return all(array).then(getReduceHandler(callback, initialValue));
    }

    public static <R, V> Promise<R> reduce(Promise<Object[]> array, Reducer<R, V> callback, R initialValue) {
        return all(array).then(getReduceHandler(callback, initialValue));
    }

    public <R, V> Promise<R> reduce(Reducer<R, V> callback, R initialValue) {
        return all().then(getReduceHandler(callback, initialValue));
    }

    @SuppressWarnings("unchecked")
    private static <V> Func<V, Object[]> getReduceRightHandler(Reducer<V, V> callback) {
        return array -> {
            if (array == null) return null;
            int n = array.length;
            if (n == 0) return null;
            V result = (V) array[n - 1];
            for (int i = n - 2; i >= 0; --i) {
                result = callback.call(result, (V) array[i], i);
            }
            return result;
        };
    }

    public static <V> Promise<V> reduceRight(Object[] array, Reducer<V, V> callback) {
        return all(array).then(getReduceRightHandler(callback));
    }

    public static <V> Promise<V> reduceRight(Promise<Object[]> array, Reducer<V, V> callback) {
        return all(array).then(getReduceRightHandler(callback));
    }

    public <V> Promise<V> reduceRight(Reducer<V, V> callback) {
        return all().then(getReduceRightHandler(callback));
    }

    @SuppressWarnings("unchecked")
    private static <R, V> Func<R, Object[]> getReduceRightHandler(Reducer<R, V> callback, R initialValue) {
        return array -> {
            if (array == null) return initialValue;
            int n = array.length;
            if (n == 0) return initialValue;
            R result = initialValue;
            for (int i = n - 1; i >= 0; --i) {
                result = callback.call(result, (V) array[i], i);
            }
            return result;
        };
    }

    public static <R, V> Promise<R> reduceRight(Object[] array, Reducer<R, V> callback, R initialValue) {
        return all(array).then(getReduceRightHandler(callback, initialValue));
    }

    public static <R, V> Promise<R> reduceRight(Promise<Object[]> array, Reducer<R, V> callback, R initialValue) {
        return all(array).then(getReduceRightHandler(callback, initialValue));
    }

    public <R, V> Promise<R> reduceRight(Reducer<R, V> callback, R initialValue) {
        return all().then(getReduceRightHandler(callback, initialValue));
    }

    @SuppressWarnings("unchecked")
    private <R, V> void call(Callback<R, V> callback, Promise<R> next, V x) {
        try {
            if (callback instanceof Action) {
                ((Action<V>) callback).call(x);
                next.resolve(null);
            } else if (callback instanceof Func) {
                next.resolve(((Func<R, V>) callback).call(x));
            } else if (callback instanceof AsyncFunc) {
                next.resolve(((AsyncFunc<R, V>) callback).call(x));
            }
        } catch (Throwable e) {
            next.reject(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <R, V> void resolve(Callback<R, V> onfulfill, Promise<R> next, V x) {
        if (onfulfill != null) {
            call(onfulfill, next, x);
        } else {
            next.resolve(x);
        }
    }

    private <R> void reject(Callback<R, Throwable> onreject, Promise<R> next, Throwable e) {
        if (onreject != null) {
            call(onreject, next, e);
        } else {
            next.reject(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <R> void _resolve(S value) {
        if (state.compareAndSet(State.PENDING, State.FULFILLED)) {
            this.value = value;
            while (!subscribers.isEmpty()) {
                Subscriber<R, S> subscriber = (Subscriber<R, S>) subscribers.poll();
                resolve(subscriber.onfulfill, subscriber.next, value);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void resolve(Object value) {
        if (isPromise(value)) {
            resolve((Promise<S>) value);
        } else if (isThenable(value)) {
            resolve((Thenable<S>) value);
        } else {
            _resolve((S) value);
        }
    }

    public void resolve(Thenable<S> value) {
        AtomicBoolean notrun = new AtomicBoolean(true);
        Action<S> resolveFunction = y -> {
            if (notrun.compareAndSet(true, false)) {
                resolve(y);
            }
        };
        Action<Throwable> rejectFunction = e -> {
            if (notrun.compareAndSet(true, false)) {
                reject(e);
            }
        };
        try {
            value.then(resolveFunction, rejectFunction);
        } catch (Throwable e) {
            if (notrun.compareAndSet(true, false)) {
                reject(e);
            }
        }
    }

    public void resolve(Promise<S> value) {
        if (value == null) {
            _resolve(null);
        } else if (value == this) {
            reject(new TypeException("Self resolution"));
        } else {
            value.fill(this);
        }
    }

    @SuppressWarnings("unchecked")
    private <R> void _reject(Throwable e) {
        if (state.compareAndSet(State.PENDING, State.REJECTED)) {
            this.reason = e;
            while (!subscribers.isEmpty()) {
                Subscriber<R, S> subscriber = (Subscriber<R, S>) subscribers.poll();
                reject(subscriber.onreject, subscriber.next, e);
            }
        }
    }

    public void reject(Throwable e) {
        _reject(e);
    }

    public Promise<?> then(Action<S> onfulfill) {
        return then(onfulfill, null);
    }

    public <R> Promise<R> then(Func<R, S> onfulfill) {
        return then((Callback<R, S>) onfulfill, null);
    }

    public <R> Promise<R> then(AsyncFunc<R, S> onfulfill) {
        return then((Callback<R, S>) onfulfill, null);
    }

    public Promise<?> then(Action<S> onfulfill, Action<Throwable> onreject) {
        return then(onfulfill, (Callback<Void, Throwable>) onreject);
    }

    public <R> Promise<R> then(Func<R, S> onfulfill, Func<R, Throwable> onreject) {
        return then(onfulfill, (Callback<R, Throwable>) onreject);
    }

    public <R> Promise<R> then(AsyncFunc<R, S> onfulfill, Func<R, Throwable> onreject) {
        return then(onfulfill, (Callback<R, Throwable>) onreject);
    }

    public <R> Promise<R> then(AsyncFunc<R, S> onfulfill, AsyncFunc<R, Throwable> onreject) {
        return then(onfulfill, (Callback<R, Throwable>) onreject);
    }

    public <R> Promise<R> then(Func<R, S> onfulfill, AsyncFunc<R, Throwable> onreject) {
        return then(onfulfill, (Callback<R, Throwable>) onreject);
    }

    @SuppressWarnings("unchecked")
    private <R> Promise<R> then(Callback<R, S> onfulfill, Callback<R, Throwable> onreject) {
        if ((onfulfill != null) || (onreject != null)) {
            Promise<R> next = new Promise<>();
            switch (state.get()) {
                case FULFILLED:
                    resolve(onfulfill, next, value);
                    break;
                case REJECTED:
                    reject(onreject, next, reason);
                    break;
                default:
                    subscribers.offer(new Subscriber<>(onfulfill, onreject, next));
                    break;
            }
            return next;
        }
        return (Promise<R>) this;
    }

    public void done(Action<S> onfulfill) {
        done(onfulfill, null);
    }

    public void done(Action<S> onfulfill, Action<Throwable> onreject) {
        then(onfulfill, onreject).then(null, e -> {
            timer.execute(() -> {
                throw new RuntimeException(e);
            });
        });
    }

    public State getState() {
        return state.get();
    }

    public S getValue() {
        return value;
    }

    public Throwable getReason() {
        return reason;
    }

    public Promise<?> catchError(Action<Throwable> onreject) {
        return then(null, onreject);
    }

    public <R> Promise<R> catchError(Func<R, Throwable> onreject) {
        return then((Callback<R, S>) null, onreject);
    }

    public <R> Promise<R> catchError(AsyncFunc<R, Throwable> onreject) {
        return then((Callback<R, S>) null, onreject);
    }

    public Promise<?> catchError(Action<Throwable> onreject, Func<Boolean, Throwable> test) {
        return catchError((Callback<Void, Throwable>) onreject, test);
    }

    public <R> Promise<R> catchError(Func<R, Throwable> onreject, Func<Boolean, Throwable> test) {
        return catchError((Callback<R, Throwable>) onreject, test);
    }

    public <R> Promise<R> catchError(AsyncFunc<R, Throwable> onreject, Func<Boolean, Throwable> test) {
        return catchError((Callback<R, Throwable>) onreject, test);
    }

    public Promise<?> catchError(Action<Throwable> onreject, AsyncFunc<Boolean, Throwable> test) {
        return catchError((Callback<Void, Throwable>) onreject, test);
    }

    public <R> Promise<R> catchError(Func<R, Throwable> onreject, AsyncFunc<Boolean, Throwable> test) {
        return catchError((Callback<R, Throwable>) onreject, test);
    }

    public <R> Promise<R> catchError(AsyncFunc<R, Throwable> onreject, AsyncFunc<Boolean, Throwable> test) {
        return catchError((Callback<R, Throwable>) onreject, test);
    }

    @SuppressWarnings("unchecked")
    private <R> Promise<R> catchError(Callback<R, Throwable> onreject, Func<Boolean, Throwable> test) {
        if (test != null) {
            return then((Callback<R, S>) null, new AsyncFunc<R, Throwable>() {
                public Promise<R> call(Throwable e) throws Throwable {
                    if (test.call(e)) {
                        return then(null, onreject);
                    }
                    throw e;
                }
            });
        }
        return then(null, onreject);
    }

    @SuppressWarnings("unchecked")
    private <R> Promise<R> catchError(Callback<R, Throwable> onreject, AsyncFunc<Boolean, Throwable> test) {
        if (test != null) {
            return then((Callback<R, S>) null, new AsyncFunc<R, Throwable>() {
                public Promise<R> call(Throwable e) throws Throwable {
                    return test.call(e).then(new AsyncFunc<R, Boolean>() {
                        public Promise<R> call(Boolean value) throws Throwable {
                            if (value) {
                                return then(null, onreject);
                            }
                            throw e;
                        }
                    });
                }
            });
        }
        return then(null, onreject);
    }

    public void fail(Action<Throwable> onreject) {
        done(null, onreject);
    }

    public Promise<S> whenComplete(Runnable action) {
        return then(
                new Func<S, S>() {
                    public S call(S value) throws Throwable {
                        action.run();
                        return value;
                    }
                },
                new Func<S, Throwable>() {
                    public S call(Throwable e) throws Throwable {
                        action.run();
                        throw e;
                    }
                }
        );
    }

    public Promise<S> whenComplete(Action<?> action) {
        return then(
                new Func<S, S>() {
                    @SuppressWarnings("unchecked")
                    public S call(S value) throws Throwable {
                        ((Action<S>) action).call(value);
                        return value;
                    }
                },
                new Func<S, Throwable>() {
                    @SuppressWarnings("unchecked")
                    public S call(Throwable e) throws Throwable {
                        ((Action<Throwable>) action).call(e);
                        throw e;
                    }
                }
        );
    }

    @SuppressWarnings("unchecked")
    public Promise<?> complete(Action<?> oncomplete) {
        return then((Action<S>) oncomplete, (Action<Throwable>) oncomplete);
    }

    @SuppressWarnings("unchecked")
    public <R> Promise<R> complete(Func<R, ?> oncomplete) {
        return then((Func<R, S>) oncomplete, (Func<R, Throwable>) oncomplete);
    }

    @SuppressWarnings("unchecked")
    public <R> Promise<R> complete(AsyncFunc<R, ?> oncomplete) {
        return then((AsyncFunc<R, S>) oncomplete, (AsyncFunc<R, Throwable>) oncomplete);
    }

    @SuppressWarnings("unchecked")
    public void always(Action<?> oncomplete) {
        done((Action<S>) oncomplete, (Action<Throwable>) oncomplete);
    }

    public void fill(Promise<S> promise) {
        then(promise::resolve, (Action<Throwable>) promise::reject);
    }

    public Promise<S> timeout(long duration, TimeUnit timeunit, Throwable reason) {
        Promise<S> promise = new Promise<>();
        Future<?> timeoutID = timer.schedule(() -> {
            if (reason == null) {
                promise.reject(new TimeoutException("timeout"));
            } else {
                promise.reject(reason);
            }
        }, duration, timeunit);
        whenComplete(() -> timeoutID.cancel(true)).fill(promise);
        return promise;
    }

    public Promise<S> timeout(long duration, Throwable reason) {
        return timeout(duration, TimeUnit.MILLISECONDS, reason);
    }

    public Promise<S> timeout(long duration) {
        return timeout(duration, TimeUnit.MILLISECONDS, null);
    }

    public Promise<S> delay(long duration, TimeUnit timeunit) {
        Promise<S> promise = new Promise<>();
        then(value1 -> timer.schedule(() -> promise.resolve(value1), duration, timeunit),
                (Action<Throwable>) promise::reject
        );
        return promise;
    }

    public Promise<S> delay(long duration) {
        return delay(duration, TimeUnit.MILLISECONDS);
    }

    @SuppressWarnings("unchecked")
    public Promise<S> tap(Action<S> onfulfilledSideEffect) {
        return then(new Func<S, S>() {
            public S call(S value) throws Throwable {
                onfulfilledSideEffect.call(value);
                return value;
            }
        });
    }

    public Future<S> toFuture() {
        return new PromiseFuture<>(this);
    }
}
