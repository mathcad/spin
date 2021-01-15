package org.spin.core.collection;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;

/**
 * 固定大小的向量，放入元素超过容器大小后会循环覆盖
 * <p>Created by xuweinan on 2017/9/3.</p>
 *
 * @author xuweinan
 */
public class FixedVector<E> extends AbstractList<E> implements List<E>, RandomAccess, Deque<E>, Cloneable, java.io.Serializable {

    private transient final E[] elementData;

    private final int compacity;
    private int head;
    private int next;

    @SafeVarargs
    public static <E> FixedVector<E> of(E... elements) {
        return new FixedVector<>(elements.length, elements);
    }

    @SafeVarargs
    public FixedVector(int compacity, E... elements) {
        if (compacity <= 0) {
            throw new IllegalStateException("Illegal compacity declared, must be a positive number: " + compacity);
        }
        if (null == elements) {
            throw new IllegalArgumentException("elements should not be null");
        }
        this.compacity = compacity + 1;
        //noinspection unchecked
        elementData = (elements.getClass() == Object[].class) ?
            (E[]) new Object[this.compacity] :
            (E[]) Array.newInstance(elements.getClass().getComponentType(), this.compacity);
        head = 0;
        if (elements.length == 0) {
            next = 0;
        } else {
            if (elements.length > compacity) {
                System.arraycopy(elements, elements.length - compacity, elementData, 0, compacity);
                next = compacity;
            } else {
                System.arraycopy(elements, 0, elementData, 0, elements.length);
                next = elements.length;
            }

        }
    }

    @Override
    public void addFirst(E e) {
        if (compacity() == 0) {
            throw new IllegalStateException("No available space to store element, compacity is: " + compacity());
        }
        head = (head == 0 ? compacity : head) - 1;
        elementData[head] = e;

        if (head == next) {
            // full abandon tail
            next = (next == 0 ? compacity : next) - 1;
        }
    }

    @Override
    public void addLast(E e) {
        if (compacity() == 0) {
            throw new IllegalStateException("No available space to store element, compacity is: " + compacity());
        }
        elementData[next] = e;
        next = ++next % compacity;

        if (next == head) {
            // full, abandon head
            head = ++head % compacity;
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
            head = ++head % compacity;
            return e;
        }
    }

    @Override
    public E removeLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("FixedVector is empty");
        } else {
            modCount++;
            next = (next == 0 ? compacity : next) - 1;
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
            head = ++head % compacity;
            return e;
        }
    }

    @Override
    public E pollLast() {
        if (isEmpty()) {
            return null;
        } else {
            modCount++;
            next = (next == 0 ? compacity : next) - 1;
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
            return elementData[(next == 0 ? compacity : next) - 1];
        }
    }

    @Override
    public E peekFirst() {
        return head == next ? null : elementData[head];
    }

    @Override
    public E peekLast() {
        return head == next ? null : elementData[(next == 0 ? compacity : next) - 1];
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
        return (next - head + compacity) % compacity;
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
        return elementData[(head + index) % compacity];
    }

    @Override
    public E set(int index, E element) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
        index = (head + index) % compacity;
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
        index = (head + index) % compacity;
        if (index < next) {
            // ahead of cursor
            System.arraycopy(elementData, index, elementData, index + 1, next - index);
            elementData[index] = element;
            next = ++next % compacity;
            if (next == head) {
                // full, abandon head
                head = ++head % compacity;
            }
            return;
        }

        // after base
        if (next > 0) {
            System.arraycopy(elementData, 0, elementData, 1, next);
        }
        next = ++next % compacity;
        if (next == head) {
            // full, abandon head
            head = ++head % compacity;
        }
        elementData[0] = elementData[compacity - 1];
        System.arraycopy(elementData, index, elementData, index + 1, compacity - index - 1);
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
        index = (head + index) % compacity;
        E e = elementData[index];
        if (index < next) {
            System.arraycopy(elementData, index + 1, elementData, index, next - index - 1);
            --next;
        } else {
            System.arraycopy(elementData, index + 1, elementData, index, compacity - index - 1);
            if (next > 0) {
                elementData[compacity - 1] = elementData[0];
                if (next > 1) {
                    System.arraycopy(elementData, 1, elementData, 0, compacity - index - 1);
                }
                --next;
            } else {
                next = compacity - 1;
            }
        }
        elementData[next] = null;
        return e;
    }

    @Override
    public int indexOf(Object o) {
        int idx = 0;
        while (idx != size()) {
            if (Objects.equals(o, elementData[(head + idx) % compacity])) {
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
            if (Objects.equals(o, elementData[(head + idx) % compacity])) {
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
        for (int i = 0; i < compacity; i++)
            elementData[i] = null;
    }

    public int compacity() {
        return compacity - 1;
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
            System.arraycopy(elementData, head, a, 0, compacity - head);
            if (next > 0) {
                System.arraycopy(elementData, 0, a, compacity - head, next);
            }
        }
        return a;
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + compacity + ", Length: " + size();
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
                E next = get((i + head) % compacity);
                lastRet = i;
                cursor = i - 1;
                return next;
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                FixedVector.this.remove(lastRet);
                if (lastRet < cursor)
                    cursor--;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }
}
