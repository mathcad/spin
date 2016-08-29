package org.zibra.net;

import java.nio.ByteBuffer;

public final class OutPacket {
    public final ByteBuffer[] buffers = new ByteBuffer[2];
    public final Integer id;
    public final int totalLength;
    public int writeLength = 0;

    public OutPacket(ByteBuffer buffer, Integer id) {
        if (id == null) {
            buffers[0] = ByteBuffer.allocate(4);
            buffers[0].putInt(buffer.limit());
            totalLength = buffer.limit() + 4;
        } else {
            buffers[0] = ByteBuffer.allocate(8);
            buffers[0].putInt(buffer.limit() | 0x80000000);
            buffers[0].putInt(id);
            totalLength = buffer.limit() + 8;
        }
        buffers[0].flip();
        buffers[1] = buffer;
        this.id = id;
    }
}
