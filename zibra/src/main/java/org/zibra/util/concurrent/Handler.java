package org.zibra.util.concurrent;


public interface Handler<R, T> {
    R call(T element, int index) throws Throwable;
}
