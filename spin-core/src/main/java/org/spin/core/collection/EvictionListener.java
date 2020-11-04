package org.spin.core.collection;

public interface EvictionListener<K, V> {

    void onEviction(K key, V value);
}
