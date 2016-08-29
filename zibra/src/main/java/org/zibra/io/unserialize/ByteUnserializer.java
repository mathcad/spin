package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagNull;
import java.io.IOException;
import java.lang.reflect.Type;

public final class ByteUnserializer extends ByteObjectUnserializer {

    public final static ByteUnserializer instance = new ByteUnserializer();

    @Override
    public Byte read(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagNull) return 0;
        return super.read(reader, tag, type);
    }

    @Override
    public Byte read(Reader reader) throws IOException {
        return read(reader, byte.class);
    }
}