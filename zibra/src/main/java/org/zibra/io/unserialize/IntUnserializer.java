package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagNull;
import java.io.IOException;
import java.lang.reflect.Type;

public final class IntUnserializer extends IntObjectUnserializer {

    public final static IntUnserializer instance = new IntUnserializer();

    @Override
    public Integer read(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagNull) return 0;
        return super.read(reader, tag, type);
    }

    @Override
    public Integer read(Reader reader) throws IOException {
        return read(reader, int.class);
    }
}
