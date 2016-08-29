package org.zibra.io;

import org.zibra.io.serialize.Writer;
import java.io.OutputStream;

public final class ZibraWriter extends Writer {

    public ZibraWriter(OutputStream stream) {
        super(stream);
    }

    public ZibraWriter(OutputStream stream, boolean simple) {
        super(stream, simple);
    }

    public ZibraWriter(OutputStream stream, ZibraMode mode) {
        super(stream, mode);
    }

    public ZibraWriter(OutputStream stream, ZibraMode mode, boolean simple) {
        super(stream, mode, simple);
    }
}
