package org.spin.core.collection;

import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.ObjectUtils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 3个元素的元组
 * <p>Created by xuweinan on 2018/3/20.</p>
 *
 * @author xuweinan
 */
public class Triple<A, B, C> implements Tuple<Triple<C, B, A>> {
    private static final long serialVersionUID = 2477816770203568993L;

    public final A first;
    public final B second;
    public final C third;

    private Triple(final A first, final B second, C third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public static <A, B, C> Triple<A, B, C> of(final A first, final B second, final C third) {
        return new Triple<>(first, second, third);
    }

    @Override
    public Object[] toArray() {
        return CollectionUtils.ofArray(first, second, third);
    }

    @Override
    public int size() {
        return 3;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> E get(int pos) {
        switch (pos) {
            case 0:
                return (E) first;
            case 1:
                return (E) second;
            case 2:
                return (E) third;
            default:
                throw new SimplifiedException("索引超出范围[0, 2]，实际:" + pos);
        }
    }

    @Override
    public boolean contains(Object value) {
        return ObjectUtils.nullSafeEquals(first, value) || ObjectUtils.nullSafeEquals(second, value);
    }

    @Override
    public void forEachWithIndex(BiConsumer<Integer, Object> action) {
        action.accept(0, first);
        action.accept(1, second);
        action.accept(2, third);
    }

    @Override
    public void forEach(Consumer<? super Object> action) {
        action.accept(first);
        action.accept(second);
        action.accept(third);
    }

    public Triple<C, B, A> reverse() {
        return of(third, second, first);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ", " + third + ')';
    }
}
