package org.spin.core.collection;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;

/**
 * 循环顺序队列 放入元素超过容器大小后会循环覆盖
 * <p>Created by xuweinan on 2017/9/3.</p>
 *
 * @author xuweinan
 */
public class CyclicArrayDeque<E> extends AbstractList<E> implements List<E>, RandomAccess, Deque<E>, Serializable {

    private final transient E[] elementData;

    private final int capacity;
    private int head;
    private int next;

    @SafeVarargs
    public static <E> CyclicArrayDeque<E> of(E... elements) {
        return new CyclicArrayDeque<>(elements.length, elements);
    }

    @SafeVarargs
    public CyclicArrayDeque(int capacity, E... elements) {
        if (capacity <= 0) {
            throw new IllegalStateException("Illegal capacity declared, must be a positive number: " + capacity);
        }
        if (null == elements) {
            throw new IllegalArgumentException("elements should not be null");
        }
        this.capacity = capacity + 1;
        //noinspection unchecked
        elementData = (elements.getClass() == Object[].class) ? (E[]) new Object[this.capacity] : (E[]) Array.newInstance(elements.getClass().getComponentType(), this.capacity);
        head = 0;
        if (elements.length == 0) {
            next = 0;
        } else {
            if (elements.length > capacity) {
                System.arraycopy(elements, elements.length - capacity, elementData, 0, capacity);
                next = capacity;
            } else {
                System.arraycopy(elements, 0, elementData, 0, elements.length);
                next = elements.length;
            }

        }
    }

    @Override
    public void addFirst(E e) {
        if (capacity() == 0) {
            throw new IllegalStateException("No available space to store element, capacity is: " + capacity());
        }
        head = (head == 0 ? capacity : head) - 1;
        elementData[head] = e;

        if (head == next) {
            // full abandon tail
            next = (next == 0 ? capacity : next) - 1;
        }
    }

    @Override
    public void addLast(E e) {
        if (capacity() == 0) {
            throw new IllegalStateException("No available space to store element, capacity is: " + capacity());
        }
        elementData[next] = e;
        next = ++next % capacity;

        if (next == head) {
            // full, abandon head
            head = ++head % capacity;
        }
    }

    @Override
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    @Override
    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }

    @Override
    public E removeFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("FixedVector is empty");
        } else {
            modCount++;
            E e = elementData[head];
            elementData[head] = null;
            head = ++head % capacity;
            return e;
        }
    }

    @Override
    public E removeLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("FixedVector is empty");
        } else {
            modCount++;
            next = (next == 0 ? capacity : next) - 1;
            E e = elementData[next];
            elementData[next] = null;
            return e;
        }
    }

    @Override
    public E pollFirst() {
        if (isEmpty()) {
            return null;
        } else {
            modCount++;
            E e = elementData[head];
            elementData[head] = null;
            head = ++head % capacity;
            return e;
        }
    }

    @Override
    public E pollLast() {
        if (isEmpty()) {
            return null;
        } else {
            modCount++;
            next = (next == 0 ? capacity : next) - 1;
            E e = elementData[next];
            elementData[next] = null;
            return e;
        }
    }

    @Override
    public E getFirst() {
        if (head == next) {
            throw new NoSuchElementException("FixedVector is empty");
        } else {
            return elementData[head];
        }
    }

    @Override
    public E getLast() {
        if (head == next) {
            throw new NoSuchElementException("FixedVector is empty");
        } else {
            return elementData[(next == 0 ? capacity : next) - 1];
        }
    }

    @Override
    public E peekFirst() {
        return head == next ? null : elementData[head];
    }

    @Override
    public E peekLast() {
        return head == next ? null : elementData[(next == 0 ? capacity : next) - 1];
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        int i = indexOf(o);
        if (i >= 0) {
            remove(i);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        int i = lastIndexOf(o);
        if (i >= 0) {
            remove(i);
            return true;
        }
        return false;
    }

    @Override
    public boolean add(E e) {
        return offerLast(e);
    }

    @Override
    public boolean offer(E e) {
        return offerLast(e);
    }

    @Override
    public E remove() {
        return removeFirst();
    }

    @Override
    public E poll() {
        return pollFirst();
    }

    @Override
    public E element() {
        return getFirst();
    }

    @Override
    public E peek() {
        return peekFirst();
    }


    @Override
    public void push(E e) {
        addFirst(e);
    }

    @Override
    public E pop() {
        return removeFirst();
    }

    @Override
    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    @Override
    public boolean contains(Object o) {
        int i = indexOf(o);
        return i >= 0;
    }

    @Override
    public int size() {
        return (next - head + capacity) % capacity;
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new DescItr();
    }

    @Override
    public E get(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
        return elementData[(head + index) % capacity];
    }

    @Override
    public E set(int index, E element) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
        index = (head + index) % capacity;
        E previous = elementData[index];
        elementData[index] = element;
        return previous;
    }

    @Override
    public void add(int index, E element) {
        if (index < 0 || index > size()) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
        if (index == size()) {
            // tail
            addLast(element);
            return;
        }
        if (index == 0) {
            // head
            addFirst(element);
            return;
        }

        // actual idx
        index = (head + index) % capacity;
        if (index < next) {
            // ahead of cursor
            System.arraycopy(elementData, index, elementData, index + 1, next - index);
            elementData[index] = element;
            next = ++next % capacity;
            if (next == head) {
                // full, abandon head
                head = ++head % capacity;
            }
            return;
        }

        // after base
        if (next > 0) {
            System.arraycopy(elementData, 0, elementData, 1, next);
        }
        next = ++next % capacity;
        if (next == head) {
            // full, abandon head
            head = ++head % capacity;
        }
        elementData[0] = elementData[capacity - 1];
        System.arraycopy(elementData, index, elementData, index + 1, capacity - index - 1);
        elementData[index] = element;
    }

    @Override
    public E remove(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
        modCount++;
        if (index == size() - 1) {
            // tail
            return removeLast();
        }
        if (index == 0) {
            // head
            return removeFirst();
        }

        // actual idx
        index = (head + index) % capacity;
        E e = elementData[index];
        if (index < next) {
            System.arraycopy(elementData, index + 1, elementData, index, next - index - 1);
            --next;
        } else {
            System.arraycopy(elementData, index + 1, elementData, index, capacity - index - 1);
            if (next > 0) {
                elementData[capacity - 1] = elementData[0];
                if (next > 1) {
                    System.arraycopy(elementData, 1, elementData, 0, capacity - index - 1);
                }
                --next;
            } else {
                next = capacity - 1;
            }
        }
        elementData[next] = null;
        return e;
    }

    @Override
    public int indexOf(Object o) {
        int idx = 0;
        while (idx != size()) {
            if (Objects.equals(o, elementData[(head + idx) % capacity])) {
                return idx;
            }
            idx = ++idx;
        }

        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        int idx = size() - 1;
        while (idx != -1) {
            if (Objects.equals(o, elementData[(head + idx) % capacity])) {
                return idx;
            }
            idx = --idx;
        }

        return -1;
    }

    @Override
    public void clear() {
        modCount++;
        head = 0;
        next = 0;
        // clear to let GC do its work
        for (int i = 0; i < capacity; i++)
            elementData[i] = null;
    }

    public int capacity() {
        return capacity - 1;
    }

    @Override
    public boolean isEmpty() {
        return head == next;
    }

    @Override
    public Object[] toArray() {
        Object[] dest = new Object[0];
        return toArray(dest);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        if (size() == 0) {
            return a;
        }
        if (a.length < size()) {
            //noinspection unchecked
            a = (a.getClass() == Object[].class) ? (T[]) new Object[size()] : (T[]) Array.newInstance(a.getClass().getComponentType(), size());
        }
        if (head < next) {
            System.arraycopy(elementData, head, a, 0, next - head);
        } else {
            System.arraycopy(elementData, head, a, 0, capacity - head);
            if (next > 0) {
                System.arraycopy(elementData, 0, a, capacity - head, next);
            }
        }
        return a;
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + capacity + ", Length: " + size();
    }

    private class DescItr implements Iterator<E> {
        int cursor = size() - 1;
        int lastRet = -1;
        int expectedModCount = modCount;

        public boolean hasNext() {
            return cursor != 0;
        }

        public E next() {
            checkForComodification();
            try {
                int i = cursor;
                E next = get((i + head) % capacity);
                lastRet = i;
                cursor = i - 1;
                return next;
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            if (lastRet < 0) throw new IllegalStateException();
            checkForComodification();

            try {
                CyclicArrayDeque.this.remove(lastRet);
                if (lastRet < cursor) cursor--;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }

        final void checkForComodification() {
            if (modCount != expectedModCount) throw new ConcurrentModificationException();
        }
    }
}
