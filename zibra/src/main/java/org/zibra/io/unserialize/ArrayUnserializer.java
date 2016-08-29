package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagList;
import java.io.IOException;
import java.lang.reflect.Type;

public final class ArrayUnserializer extends BaseUnserializer {

    public final static ArrayUnserializer instance = new ArrayUnserializer();

    @Override
    public Object unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagList) return ReferenceReader.readArray(reader, type);
        return super.unserialize(reader, tag, type);
    }
}
