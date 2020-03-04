package org.spin.core.util;

import org.spin.core.collection.Pair;

import java.util.Enumeration;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 流操作相关
 * <p>Created by xuweinan on 2016/9/1.
 *
 * @author xuweinan
 */
public interface StreamUtils {

    /**
     * 将enumeration转换为流
     *
     * @param e   可枚举对象
     * @param <T> 对象类型
     * @return 流
     */
    static <T> Stream<T> stream(Enumeration<T> e) {
        return StreamSupport.stream(new Spliterators.AbstractSpliterator<T>(Long.MAX_VALUE, Spliterator.ORDERED) {

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                if (e.hasMoreElements()) {
                    action.accept(e.nextElement());
                    return true;
                }
                return false;
            }
        }, false);
    }

    /**
     * 将Iterable转换为流
     *
     * @param iterable 可迭代对象
     * @param <T>      对象类型
     * @return 流
     */
    static <T> Stream<T> stream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

}
