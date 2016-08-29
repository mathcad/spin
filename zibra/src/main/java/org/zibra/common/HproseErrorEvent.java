package org.zibra.common;

public interface HproseErrorEvent {
    void handler(String name, Throwable error);
}
