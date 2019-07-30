package org.spin.core.collection;

import org.spin.core.throwable.SpinException;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.ObjectUtils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 6个元素的元组
 * <p>Created by xuweinan on 2018/3/20.</p>
 *
 * @author xuweinan
 */
public class Tuple6<A, B, C, D, E, F> implements Tuple<Tuple6<F, E, D, C, B, A>> {
    private static final long serialVersionUID = 4338452880557297289L;

    public final A c1;
    public final B c2;
    public final C c3;
    public final D c4;
    public final E c5;
    public final F c6;

    private Tuple6(final A c1, final B c2, final C c3, final D c4, final E c5, final F c6) {
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        this.c4 = c4;
        this.c5 = c5;
        this.c6 = c6;
    }

    public static <A, B, C, D, E, F> Tuple6<A, B, C, D, E, F> of(final A c1, final B c2, final C c3, final D c4, final E c5, final F c6) {
        return new Tuple6<>(c1, c2, c3, c4, c5, c6);
    }

    @Override
    public Object[] toArray() {
        return CollectionUtils.ofArray(c1, c2, c3, c4, c5, c6);
    }

    @Override
    public int size() {
        return 6;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E1> E1 get(int pos) {
        switch (pos) {
            case 0:
                return (E1) c1;
            case 1:
                return (E1) c2;
            case 2:
                return (E1) c3;
            case 3:
                return (E1) c4;
            case 4:
                return (E1) c5;
            case 5:
                return (E1) c6;
            default:
                throw new IndexOutOfBoundsException("索引超出范围[0, 5]，实际:" + pos);
        }
    }

    @Override
    public boolean contains(Object value) {
        return ObjectUtils.nullSafeEquals(c1, value)
            || ObjectUtils.nullSafeEquals(c2, value)
            || ObjectUtils.nullSafeEquals(c3, value)
            || ObjectUtils.nullSafeEquals(c4, value)
            || ObjectUtils.nullSafeEquals(c5, value)
            || ObjectUtils.nullSafeEquals(c6, value);
    }

    @Override
    public void forEachWithIndex(BiConsumer<Integer, Object> action) {
        action.accept(0, c1);
        action.accept(1, c2);
        action.accept(2, c3);
        action.accept(3, c4);
        action.accept(4, c5);
        action.accept(5, c6);
    }

    @Override
    public void forEach(Consumer<? super Object> action) {
        action.accept(c1);
        action.accept(c2);
        action.accept(c3);
        action.accept(c4);
        action.accept(c5);
        action.accept(c6);
    }

    public Tuple6<F, E, D, C, B, A> reverse() {
        return of(c6, c5, c4, c3, c2, c1);
    }

    @Override
    public String toString() {
        return "(" + c1 + ", " + c2 + ", " + c3 + ", " + c4 + ", " + c5 + ", " + c5 + ')';
    }
}
