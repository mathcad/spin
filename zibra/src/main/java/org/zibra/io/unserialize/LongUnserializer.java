package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagNull;
import java.io.IOException;
import java.lang.reflect.Type;

public final class LongUnserializer extends LongObjectUnserializer {

    public final static LongUnserializer instance = new LongUnserializer();

    @Override
    public Long read(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagNull) return 0L;
        return super.read(reader, tag, type);
    }

    @Override
    public Long read(Reader reader) throws IOException {
        return read(reader, long.class);
    }
}
