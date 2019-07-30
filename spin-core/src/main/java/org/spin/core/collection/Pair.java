package org.spin.core.collection;

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

    public final A c1;
    public final B c2;

    private Pair(final A c1, final B c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public static <A, B> Pair<A, B> of(final A c1, final B second) {
        return new Pair<>(c1, second);
    }

    @Override
    public Object[] toArray() {
        return CollectionUtils.ofArray(c1, c2);
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
                return (E) c1;
            case 1:
                return (E) c2;
            default:
                throw new IndexOutOfBoundsException("索引超出范围[0, 1]，实际:" + pos);
        }
    }

    @Override
    public boolean contains(Object value) {
        return ObjectUtils.nullSafeEquals(c1, value) || ObjectUtils.nullSafeEquals(c2, value);
    }

    @Override
    public void forEachWithIndex(BiConsumer<Integer, Object> action) {
        action.accept(0, c1);
        action.accept(1, c2);
    }

    @Override
    public void forEach(Consumer<? super Object> action) {
        action.accept(c1);
        action.accept(c2);
    }

    public Pair<B, A> reverse() {
        return of(c2, c1);
    }

    @Override
    public String toString() {
        return "(" + c1 + ", " + c2 + ')';
    }
}
