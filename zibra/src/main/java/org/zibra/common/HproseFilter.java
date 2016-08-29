package org.zibra.common;

import java.nio.ByteBuffer;

public interface HproseFilter {
    ByteBuffer inputFilter(ByteBuffer data, ZibraContext context);
    ByteBuffer outputFilter(ByteBuffer data, ZibraContext context);
}