package org.zibra.common;

import org.zibra.util.concurrent.Promise;

import java.nio.ByteBuffer;

public interface FilterHandler {
    Promise<ByteBuffer> handle(ByteBuffer request, ZibraContext context, NextFilterHandler next);
}
