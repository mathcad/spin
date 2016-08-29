package org.zibra.net;

import java.nio.ByteBuffer;

public interface ReceiveCallback {
    void handler(ByteBuffer buffer, Throwable e);
}
