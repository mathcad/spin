package org.spin.core.collection;

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

    public final A c1;
    public final B c2;
    public final C c3;

    private Triple(final A c1, final B c2, C c3) {
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
    }

    public static <A, B, C> Triple<A, B, C> of(final A c1, final B c2, final C c3) {
        return new Triple<>(c1, c2, c3);
    }

    @Override
    public Object[] toArray() {
        return CollectionUtils.ofArray(c1, c2, c3);
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
                return (E) c1;
            case 1:
                return (E) c2;
            case 2:
                return (E) c3;
            default:
                throw new IndexOutOfBoundsException("索引超出范围[0, 2]，实际:" + pos);
        }
    }

    @Override
    public boolean contains(Object value) {
        return ObjectUtils.nullSafeEquals(c1, value)
            || ObjectUtils.nullSafeEquals(c2, value)
            || ObjectUtils.nullSafeEquals(c3, value);
    }

    @Override
    public void forEachWithIndex(BiConsumer<Integer, Object> action) {
        action.accept(0, c1);
        action.accept(1, c2);
        action.accept(2, c3);
    }

    @Override
    public void forEach(Consumer<? super Object> action) {
        action.accept(c1);
        action.accept(c2);
        action.accept(c3);
    }

    public Triple<C, B, A> reverse() {
        return of(c3, c2, c1);
    }

    @Override
    public String toString() {
        return "(" + c1 + ", " + c2 + ", " + c3 + ')';
    }
}
