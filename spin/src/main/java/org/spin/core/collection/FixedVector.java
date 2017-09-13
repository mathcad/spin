package org.spin.core.collection;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * 固定大小的向量，放入元素超过容器大小后会循环覆盖
 * <p>Created by xuweinan on 2017/9/3.</p>
 *
 * @author xuweinan
 */
public class FixedVector<T> implements Collection<T> {

    private transient final Object[] elementData;

    private int size;
    private int base;
    private int cursor;

    public FixedVector(int size) {
        this.size = ++size;
        this.elementData = new Object[this.size];
        base = 0;
        cursor = 0;
    }

    public void put(T element) {
        elementData[cursor] = element;
        cursor = ++cursor;
        if (cursor >= size) {
            cursor %= size;
            base = ++base % size;
        }
    }

    public T get() {
        return get(base);
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        //noinspection unchecked
        return base == cursor ? null : (T) elementData[base];
    }

    public T peek() {
        return get(cursor - 1);
    }

    public T pop() {
        if (base == cursor) {
            throw new IllegalStateException("Vector is empty");
        }
        cursor = (--cursor + size) % size;
        //noinspection unchecked
        return (T) elementData[cursor];
    }

    @Override
    public void clear() {
        base = 0;
        cursor = 0;
    }

    @Override
    public int size() {
        return size - 1;
    }

    public int length() {
        return (cursor - base + size) % size;
    }

    public boolean isEmpty() {
        return base != cursor;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(elementData, size - 1);
    }

    @Override
    public <E> E[] toArray(E[] a) {
        if (a.length < size - 1)
            //noinspection unchecked
            return (E[]) Arrays.copyOf(elementData, size - 1, a.getClass());
        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(elementData, 0, a, 0, size);
        if (a.length > size)
            a[size] = null;
        return a;
    }

    @Override
    public boolean add(T element) {
        elementData[cursor] = element;
        cursor = ++cursor;
        if (cursor >= size) {
            cursor %= size;
            base = ++base % size;
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }
}
