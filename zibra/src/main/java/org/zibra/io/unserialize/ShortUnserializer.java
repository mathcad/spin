package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagNull;
import java.io.IOException;
import java.lang.reflect.Type;

public final class ShortUnserializer extends ShortObjectUnserializer {

    public final static ShortUnserializer instance = new ShortUnserializer();

    @Override
    public Short read(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagNull) return 0;
        return super.read(reader, tag, type);
    }

    @Override
    public Short read(Reader reader) throws IOException {
        return read(reader, short.class);
    }
}
