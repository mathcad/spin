package org.spin.core.collection;

import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.ObjectUtils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 两个元素的元组
 * <p>Created by xuweinan on 2018/3/20.</p>
 *
 * @author xuweinan
 */
public class Pair<A, B> implements Tuple<Pair<B, A>> {
    private static final long serialVersionUID = 5250117296300174625L;

    public final A first;
    public final B second;

    private Pair(final A first, final B second) {
        this.first = first;
        this.second = second;
    }

    public static <A, B> Pair<A, B> of(final A first, final B second) {
        return new Pair<>(first, second);
    }

    @Override
    public Object[] toArray() {
        return CollectionUtils.ofArray(first, second);
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> E get(int pos) {
        switch (pos) {
            case 0:
                return (E) first;
            case 1:
                return (E) second;
            default:
                throw new SimplifiedException("索引超出范围[0, 1]，实际:" + pos);
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
    }

    public Pair<B, A> reverse() {
        return of(second, first);
    }

    @Override
    public void forEach(Consumer<? super Object> action) {
        action.accept(first);
        action.accept(second);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ')';
    }
}
