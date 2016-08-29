package org.zibra.common;

import org.zibra.util.concurrent.Promise;

import java.nio.ByteBuffer;

public interface NextFilterHandler {
    Promise<ByteBuffer> handle(ByteBuffer request, ZibraContext context);
}
