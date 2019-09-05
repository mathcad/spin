package org.spin.core.collection;

import org.spin.core.util.CollectionUtils;
import org.spin.core.util.ObjectUtils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 20个元素的元组
 * <p>Created by xuweinan on 2018/3/20.</p>
 *
 * @author xuweinan
 */
public class Tuple20<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T> implements
    Tuple<Tuple20<T, S, R, Q, P, O, N, M, L, K, J, I, H, G, F, E, D, C, B, A>> {
    private static final long serialVersionUID = 8411203531002782901L;

    public final A c1;
    public final B c2;
    public final C c3;
    public final D c4;
    public final E c5;
    public final F c6;
    public final G c7;
    public final H c8;
    public final I c9;
    public final J c10;
    public final K c11;
    public final L c12;
    public final M c13;
    public final N c14;
    public final O c15;
    public final P c16;
    public final Q c17;
    public final R c18;
    public final S c19;
    public final T c20;

    private Tuple20(final A c1, final B c2, final C c3, final D c4,
                    final E c5, final F c6, final G c7, final H c8,
                    final I c9, final J c10, final K c11, final L c12,
                    final M c13, final N c14, final O c15, final P c16,
                    final Q c17, final R c18, final S c19, final T c20) {
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        this.c4 = c4;
        this.c5 = c5;
        this.c6 = c6;
        this.c7 = c7;
        this.c8 = c8;
        this.c9 = c9;
        this.c10 = c10;
        this.c11 = c11;
        this.c12 = c12;
        this.c13 = c13;
        this.c14 = c14;
        this.c15 = c15;
        this.c16 = c16;
        this.c17 = c17;
        this.c18 = c18;
        this.c19 = c19;
        this.c20 = c20;
    }

    public static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T>
    Tuple20<A, B, C, D, E, F, G, H, I, J, K, L,
        M, N, O, P, Q, R, S, T> of(final A c1, final B c2, final C c3, final D c4,
                                   final E c5, final F c6, final G c7, final H c8,
                                   final I c9, final J c10, final K c11, final L c12,
                                   final M c13, final N c14, final O c15, final P c16,
                                   final Q c17, final R c18, final S c19, final T c20) {
        return new Tuple20<>(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15, c16, c17, c18, c19, c20);
    }

    @Override
    public Object[] toArray() {
        return CollectionUtils.ofArray(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15, c16, c17, c18, c19, c20);
    }

    @Override
    public int size() {
        return 20;
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
            case 6:
                return (E1) c7;
            case 7:
                return (E1) c8;
            case 8:
                return (E1) c9;
            case 9:
                return (E1) c10;
            case 10:
                return (E1) c11;
            case 11:
                return (E1) c12;
            case 12:
                return (E1) c13;
            case 13:
                return (E1) c14;
            case 14:
                return (E1) c15;
            case 15:
                return (E1) c16;
            case 16:
                return (E1) c17;
            case 17:
                return (E1) c18;
            case 18:
                return (E1) c19;
            case 19:
                return (E1) c20;
            default:
                throw new IndexOutOfBoundsException("索引超出范围[0, 19]，实际:" + pos);
        }
    }

    @Override
    public boolean contains(Object value) {
        return ObjectUtils.nullSafeEquals(c1, value)
            || ObjectUtils.nullSafeEquals(c2, value)
            || ObjectUtils.nullSafeEquals(c3, value)
            || ObjectUtils.nullSafeEquals(c4, value)
            || ObjectUtils.nullSafeEquals(c5, value)
            || ObjectUtils.nullSafeEquals(c6, value)
            || ObjectUtils.nullSafeEquals(c7, value)
            || ObjectUtils.nullSafeEquals(c8, value)
            || ObjectUtils.nullSafeEquals(c9, value)
            || ObjectUtils.nullSafeEquals(c10, value)
            || ObjectUtils.nullSafeEquals(c11, value)
            || ObjectUtils.nullSafeEquals(c12, value)
            || ObjectUtils.nullSafeEquals(c13, value)
            || ObjectUtils.nullSafeEquals(c14, value)
            || ObjectUtils.nullSafeEquals(c15, value)
            || ObjectUtils.nullSafeEquals(c16, value)
            || ObjectUtils.nullSafeEquals(c17, value)
            || ObjectUtils.nullSafeEquals(c18, value)
            || ObjectUtils.nullSafeEquals(c19, value)
            || ObjectUtils.nullSafeEquals(c20, value);
    }

    @Override
    public void forEachWithIndex(BiConsumer<Integer, Object> action) {
        action.accept(0, c1);
        action.accept(1, c2);
        action.accept(2, c3);
        action.accept(3, c4);
        action.accept(4, c5);
        action.accept(5, c6);
        action.accept(6, c7);
        action.accept(7, c8);
        action.accept(8, c9);
        action.accept(9, c10);
        action.accept(10, c11);
        action.accept(11, c12);
        action.accept(12, c13);
        action.accept(13, c14);
        action.accept(14, c15);
        action.accept(15, c16);
        action.accept(16, c17);
        action.accept(17, c18);
        action.accept(18, c19);
        action.accept(19, c20);
    }

    @Override
    public void forEach(Consumer<? super Object> action) {
        action.accept(c1);
        action.accept(c2);
        action.accept(c3);
        action.accept(c4);
        action.accept(c5);
        action.accept(c6);
        action.accept(c7);
        action.accept(c8);
        action.accept(c9);
        action.accept(c10);
        action.accept(c11);
        action.accept(c12);
        action.accept(c13);
        action.accept(c14);
        action.accept(c15);
        action.accept(c16);
        action.accept(c17);
        action.accept(c18);
        action.accept(c19);
        action.accept(c20);
    }

    public Tuple20<T, S, R, Q, P, O, N, M, L, K, J, I, H, G, F, E, D, C, B, A> reverse() {
        return of(c20, c19, c18, c17, c16, c15, c14, c13, c12, c11, c10, c9, c8, c7, c6, c5, c4, c3, c2, c1);
    }

    @Override
    public String toString() {
        return "("
            + c1 + ", "
            + c2 + ", "
            + c3 + ", "
            + c4 + ", "
            + c5 + ", "
            + c6 + ", "
            + c7 + ", "
            + c8 + ", "
            + c9 + ", "
            + c10 + ", "
            + c11 + ", "
            + c12 + ", "
            + c13 + ", "
            + c14 + ", "
            + c15 + ", "
            + c16 + ", "
            + c17 + ", "
            + c18 + ", "
            + c19 + ", "
            + c20 +
            ')';
    }
}
