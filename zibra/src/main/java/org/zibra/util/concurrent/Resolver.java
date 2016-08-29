package org.zibra.util.concurrent;

public interface Resolver<V> {
    void resolve(V value);
    void resolve(Promise<V> value);
}
