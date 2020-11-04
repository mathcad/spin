package org.spin.core.collection;

public interface Weigher<V> {

    int weightOf(V value);

    default <K> int weightOfEntry(K key, V value) {
        return weightOf(value);
    }
}
