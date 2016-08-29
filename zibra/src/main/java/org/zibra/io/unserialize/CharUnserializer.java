package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagNull;
import java.io.IOException;
import java.lang.reflect.Type;

public final class CharUnserializer extends CharObjectUnserializer {

    public final static CharUnserializer instance = new CharUnserializer();

    @Override
    public Character read(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagNull) return (char)0;
        return super.read(reader, tag, type);
    }

    @Override
    public Character read(Reader reader) throws IOException {
        return read(reader, char.class);
    }
}
