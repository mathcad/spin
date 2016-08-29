package org.zibra.io;

import org.zibra.io.unserialize.Reader;
import java.io.InputStream;
import java.nio.ByteBuffer;

public final class ZibraReader extends Reader {

    public ZibraReader(InputStream stream) {
        super(stream);
    }

    public ZibraReader(InputStream stream, boolean simple) {
        super(stream, simple);
    }

    public ZibraReader(InputStream stream, ZibraMode mode) {
        super(stream, mode);
    }

    public ZibraReader(InputStream stream, ZibraMode mode, boolean simple) {
        super(stream, mode, simple);
    }

    public ZibraReader(ByteBuffer buffer) {
        super(buffer);
    }

    public ZibraReader(ByteBuffer buffer, boolean simple) {
        super(buffer, simple);
    }

    public ZibraReader(ByteBuffer buffer, ZibraMode mode) {
        super(buffer, mode);
    }

    public ZibraReader(ByteBuffer buffer, ZibraMode mode, boolean simple) {
        super(buffer, mode, simple);
    }

    public ZibraReader(byte[] bytes) {
        super(bytes);
    }

    public ZibraReader(byte[] bytes, boolean simple) {
        super(bytes, simple);
    }

    public ZibraReader(byte[] bytes, ZibraMode mode) {
        super(bytes, mode);
    }

    public ZibraReader(byte[] bytes, ZibraMode mode, boolean simple) {
        super(bytes, mode, simple);
    }

}