package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagNull;
import java.io.IOException;
import java.lang.reflect.Type;

public final class FloatUnserializer extends FloatObjectUnserializer {

    public final static FloatUnserializer instance = new FloatUnserializer();

    @Override
    public Float read(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagNull) return 0.0f;
        return super.read(reader, tag, type);
    }

    @Override
    public Float read(Reader reader) throws IOException {
        return read(reader, float.class);
    }
}
