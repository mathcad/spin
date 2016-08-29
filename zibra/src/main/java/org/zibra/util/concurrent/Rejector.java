package org.zibra.util.concurrent;

public interface Rejector {
    void reject(Throwable e);
}
