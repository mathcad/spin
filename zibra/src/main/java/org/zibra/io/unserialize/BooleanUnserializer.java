package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagNull;
import java.io.IOException;
import java.lang.reflect.Type;

public final class BooleanUnserializer extends BooleanObjectUnserializer {

    public final static BooleanUnserializer instance = new BooleanUnserializer();

    @Override
    public Boolean read(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagNull) return false;
        return super.read(reader, tag, type);
    }

    @Override
    public Boolean read(Reader reader) throws IOException {
        return read(reader, boolean.class);
    }
}
