package org.spin.collection;

import org.spin.throwable.SimplifiedException;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.function.Consumer;

/**
 * Matrix中的一行
 *
 * @author xuweinan
 */
public class ArrayRow<E> implements Row<E>, RandomAccess, Cloneable, Serializable {
    private static final long serialVersionUID = 3367436433132931338L;

    private RowUpdateListener observer;

    private transient Object[] elementData;

    private final int size;

    private int rownum = -1;

    /**
     * The number of times this list has been <i>structurally modified</i>.
     * Structural modifications are those that change the size of the
     * list, or otherwise perturb it in such a fashion that iterations in
     * progress may yield incorrect results.
     * <p>
     * <p>This field is used by the iterator and list iterator implementation
     * returned by the {@code iterator} and {@code listIterator} methods.
     * If the value of this field changes unexpectedly, the iterator (or list
     * iterator) will throw a {@code ConcurrentModificationException} in
     * response to the {@code next}, {@code remove}, {@code previous},
     * {@code set} or {@code add} operations.  This provides
     * <i>fail-fast</i> behavior, rather than non-deterministic behavior in
     * the face of concurrent modification during iteration.
     * <p>
     * <p><b>Use of this field by subclasses is optional.</b> If a subclass
     * wishes to provide fail-fast iterators (and list iterators), then it
     * merely has to increment this field in its {@code add(int, E)} and
     * {@code remove(int)} methods (and any other methods that it overrides
     * that result in structural modifications to the list).  A single call to
     * {@code add(int, E)} or {@code remove(int)} must add no more than
     * one to this field, or the iterators (and list iterators) will throw
     * bogus {@code ConcurrentModificationExceptions}.  If an implementation
     * does not wish to provide fail-fast iterators, this field may be
     * ignored.
     */
    private transient int modCount = 0;

    public ArrayRow(int initialCapacity) {
        if (initialCapacity > 0) {
            this.elementData = new Object[initialCapacity];
            size = initialCapacity;
        } else {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
    }

    public ArrayRow(Collection<? extends E> c) {
        elementData = c.toArray();
        if ((size = elementData.length) != 0) {
            if (elementData.getClass() != Object[].class)
                elementData = Arrays.copyOf(elementData, size, Object[].class);
        } else {
            this.elementData = new Object[]{};
        }
    }

    @Override
    public int rownum() {
        return rownum;
    }

    @Override
    public void setRownum(int rownum) {
        this.rownum = rownum;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public int indexOf(Object o) {
        if (o == null) {
            for (int i = 0; i < size; i++)
                if (elementData[i] == null)
                    return i;
        } else {
            for (int i = 0; i < size; i++)
                if (o.equals(elementData[i]))
                    return i;
        }
        return -1;
    }

    @Override
    public E get(int index) {
        rangeCheckForAdd(index);

        return elementData(index);
    }

    @Override
    public E set(int index, E element) {
        rangeCheckForAdd(index);

        E oldValue = elementData(index);
        elementData[index] = element;
        RowUpdateEvent event = new RowUpdateEvent(this);
        event.setUpdateCols(index);
        notifyModified(event);
        return oldValue;
    }

    @Override
    public E delete(int index) {
        return set(index, null);
    }

    @Override
    public void clear() {
        // clear to let GC do its work
        RowUpdateEvent event = new RowUpdateEvent(this);
        for (int i = 0; i < size; i++) {
            elementData[i] = null;
            event.addUpdateCols(i);
        }
        notifyModified(event);
    }

    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        final int expectedModCount = modCount;
        @SuppressWarnings("unchecked") final E[] elementData = (E[]) this.elementData;
        final int size = this.size;
        for (int i = 0; modCount == expectedModCount && i < size; i++) {
            action.accept(elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    public void setUpdateLestener(RowUpdateListener listener) {
        this.observer = listener;
    }

    private void notifyModified(RowUpdateEvent event) {
        if (null != observer) {
            observer.updated(event);
        }
    }

    @SuppressWarnings("unchecked")
    private E elementData(int index) {
        return (E) elementData[index];
    }

    private void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + size;
    }

    private class Itr implements Iterator<E> {
        int cursor;       // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such
        int expectedModCount = modCount;

        @Override
        public boolean hasNext() {
            return cursor != size;
        }

        @Override
        public E next() {
            checkForComodification();
            int i = cursor;
            if (i >= size)
                throw new NoSuchElementException();
            Object[] elementData = ArrayRow.this.elementData;
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i + 1;
            return elementData(lastRet = i);
        }

        @Override
        public void remove() {
            throw new SimplifiedException("Row集合不支持移除操作");
        }

        @Override
        public void forEachRemaining(Consumer<? super E> consumer) {
            Objects.requireNonNull(consumer);
            final int size = ArrayRow.this.size;
            int i = cursor;
            if (i >= size) {
                return;
            }
            final Object[] elementData = ArrayRow.this.elementData;
            if (i >= elementData.length) {
                throw new ConcurrentModificationException();
            }
            while (i != size && modCount == expectedModCount) {
                consumer.accept(elementData(i++));
            }
            // update once at end of iteration to reduce heap write traffic
            cursor = i;
            lastRet = i - 1;
            checkForComodification();
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }
}