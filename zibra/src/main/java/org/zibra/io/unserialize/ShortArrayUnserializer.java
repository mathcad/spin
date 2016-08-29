package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagList;
import java.io.IOException;
import java.lang.reflect.Type;

public final class ShortArrayUnserializer extends BaseUnserializer<short[]> {

    public final static ShortArrayUnserializer instance = new ShortArrayUnserializer();

    @Override
    public short[] unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagList) return ReferenceReader.readShortArray(reader);
        if (tag == TagEmpty) return new short[0];
        return super.unserialize(reader, tag, type);
    }

    public short[] read(Reader reader) throws IOException {
        return read(reader, short[].class);
    }
}
