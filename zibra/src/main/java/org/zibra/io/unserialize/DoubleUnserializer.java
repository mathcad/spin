package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagNull;
import java.io.IOException;
import java.lang.reflect.Type;

public final class DoubleUnserializer extends DoubleObjectUnserializer {

    public final static DoubleUnserializer instance = new DoubleUnserializer();

    @Override
    public Double read(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagNull) return 0.0;
        return super.read(reader, tag, type);
    }

    @Override
    public Double read(Reader reader) throws IOException {
        return read(reader, double.class);
    }
}
