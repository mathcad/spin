package org.infrastructure.util;

import java.util.Enumeration;
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
public class StreamUtils {
    public static <T> Stream<T> enumerationAsStream(Enumeration<T> e) {
        return StreamSupport.stream(
                new Spliterators.AbstractSpliterator<T>(Long.MAX_VALUE, Spliterator.ORDERED) {
                    public boolean tryAdvance(Consumer<? super T> action) {
                        if (e.hasMoreElements()) {
                            action.accept(e.nextElement());
                            return true;
                        }
                        return false;
                    }

                    public void forEachRemaining(Consumer<? super T> action) {
                        while (e.hasMoreElements()) action.accept(e.nextElement());
                    }
                }, false);
    }
}