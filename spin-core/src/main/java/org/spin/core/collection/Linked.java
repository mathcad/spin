package org.spin.core.collection;

import java.util.Deque;

/**
 * An element that is linked on the {@link Deque}.
 */
interface Linked<T extends Linked<T>> {

    T getPrevious();

    void setPrevious(T prev);

    T getNext();

    void setNext(T next);
}
