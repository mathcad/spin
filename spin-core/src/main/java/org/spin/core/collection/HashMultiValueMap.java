/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spin.core.collection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple implementation of {@link MultiValueMap} that wraps a {@link HashMap},
 * storing multiple values in a {@link ArrayList}.
 * <p>This Map implementation is generally not thread-safe. It is primarily designed
 * for data structures exposed from request objects, for use in a single thread only.
 *
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @since 3.0
 */
public class HashMultiValueMap<K, V> implements MultiValueMap<K, V>, Serializable {

    private static final long serialVersionUID = 3801124242820219131L;

    private final Map<K, List<V>> targetMap;


    /**
     * Create a new HashMultiValueMap that wraps a {@link HashMap}.
     */
    public HashMultiValueMap() {
        this.targetMap = new HashMap<>();
    }

    /**
     * Create a new HashMultiValueMap that wraps a {@link HashMap}
     * with the given initial capacity.
     *
     * @param initialCapacity the initial capacity
     */
    public HashMultiValueMap(int initialCapacity) {
        this.targetMap = new HashMap<>(initialCapacity);
    }

    /**
     * Copy constructor: Create a new HashMultiValueMap with the same mappings as
     * the specified Map. Note that this will be a shallow copy; its value-holding
     * List entries will get reused and therefore cannot get modified independently.
     *
     * @param otherMap the Map whose mappings are to be placed in this Map
     * @see #clone()
     * @see #deepCopy()
     */
    public HashMultiValueMap(Map<K, List<V>> otherMap) {
        this.targetMap = new HashMap<>(otherMap);
    }


    // MultiValueMap implementation

    @Override
    public void add(K key, V value) {
        List<V> values = this.targetMap.computeIfAbsent(key, k -> new ArrayList<>());
        values.add(value);
    }

    @Override
    public V getFirst(K key) {
        List<V> values = this.targetMap.get(key);
        return (values != null ? values.get(0) : null);
    }

    @Override
    public void set(K key, V value) {
        List<V> values = new ArrayList<>();
        values.add(value);
        this.targetMap.put(key, values);
    }

    @Override
    public void setAll(Map<K, V> values) {
        for (Entry<K, V> entry : values.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Map<K, V> toSingleValueMap() {
        HashMap<K, V> singleValueMap = new HashMap<>(this.targetMap.size());
        for (Entry<K, List<V>> entry : this.targetMap.entrySet()) {
            singleValueMap.put(entry.getKey(), entry.getValue().get(0));
        }
        return singleValueMap;
    }


    // Map implementation

    @Override
    public int size() {
        return this.targetMap.size();
    }

    @Override
    public boolean isEmpty() {
        return this.targetMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.targetMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.targetMap.containsValue(value);
    }

    @Override
    public List<V> get(Object key) {
        return this.targetMap.get(key);
    }

    @Override
    public List<V> put(K key, List<V> value) {
        return this.targetMap.put(key, value);
    }

    @Override
    public List<V> remove(Object key) {
        return this.targetMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends List<V>> map) {
        this.targetMap.putAll(map);
    }

    @Override
    public void clear() {
        this.targetMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return this.targetMap.keySet();
    }

    @Override
    public Collection<List<V>> values() {
        return this.targetMap.values();
    }

    @Override
    public Set<Entry<K, List<V>>> entrySet() {
        return this.targetMap.entrySet();
    }

    /**
     * Create a deep copy of this Map.
     *
     * @return a copy of this Map, including a copy of each value-holding List entry
     * @see #clone()
     * @since 4.2
     */
    public HashMultiValueMap<K, V> deepCopy() {
        HashMultiValueMap<K, V> copy = new HashMultiValueMap<>(this.targetMap.size());
        for (Map.Entry<K, List<V>> entry : this.targetMap.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }


    @Override
    public boolean equals(Object obj) {
        return this.targetMap.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.targetMap.hashCode();
    }

    @Override
    public String toString() {
        return this.targetMap.toString();
    }

}
