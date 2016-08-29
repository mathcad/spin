package org.zibra.util;

import java.util.HashMap;

public class CaseInsensitiveMap<K, V> extends HashMap<K, V> {
    private final HashMap<String, K> caseInsensitiveKeys;

    public CaseInsensitiveMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        this.caseInsensitiveKeys = new HashMap<>();
    }

    public CaseInsensitiveMap(int initialCapacity) {
        super(initialCapacity);
        this.caseInsensitiveKeys = new HashMap<>();
    }

    public CaseInsensitiveMap() {
        super();
        this.caseInsensitiveKeys = new HashMap<>();
    }

    private String convertKey(Object key) {
        return ((key instanceof char[]) ? new String((char[]) key) : key.toString()).toLowerCase();
    }

    @Override
    public V put(K key, V value) {
        this.caseInsensitiveKeys.put(convertKey(key), key);
        return super.put(key, value);
    }

    @Override
    public boolean containsKey(Object key) {
        return (key instanceof String && this.caseInsensitiveKeys.containsKey(convertKey(key)));
    }

    @Override
    public V get(Object key) {
        return super.get(this.caseInsensitiveKeys.get(convertKey(key)));
    }

    @Override
    public V remove(Object key) {
        return super.remove(this.caseInsensitiveKeys.remove(convertKey(key)));
    }

    @Override
    public void clear() {
        this.caseInsensitiveKeys.clear();
        super.clear();
    }
}