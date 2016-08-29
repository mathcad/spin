package org.zibra.util.concurrent;

public interface Executor<V> {
    void exec(Resolver<V> resolver, Rejector rejector);
}
