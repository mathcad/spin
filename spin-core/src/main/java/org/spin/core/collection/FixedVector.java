package org.spin.core.collection;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;

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

    public static <T> FixedVector<T> of(T t1) {
        FixedVector<T> v = new FixedVector<>(1);
        v.put(t1);
        return v;
    }

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

    /**
     * 获取第一个元素
     */
    public T get() {
        //noinspection unchecked
        return base == cursor ? null : (T) elementData[base];
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        //noinspection unchecked
        return base == cursor ? null : (T) elementData[(base + index) % size];
    }

    /**
     * 获取最后一个元素
     */
    public T peek() {
        return get(cursor - 1);
    }

    /**
     * 将最后一个元素出栈
     */
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

    @Override
    public boolean isEmpty() {
        return base != cursor;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return new VectorItr();
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

    private class VectorItr implements Iterator<T> {
        // TODO: 需要实现迭代器
        int cursor;       // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public T next() {
            return null;
        }

        @Override
        public void remove() {

        }

        @Override
        public void forEachRemaining(Consumer<? super T> action) {

        }
    }

    @Override
    public String toString() {
        return "FixedVector{" +
            "elementData=" + Arrays.toString(elementData) +
            '}';
    }
}
