package org.zibra.util.concurrent;


public interface Reducer<R, T> {
    R call(R prev, T element, int index) throws Throwable;
}
