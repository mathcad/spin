package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagList;
import java.io.IOException;
import java.lang.reflect.Type;

public final class LongArrayUnserializer extends BaseUnserializer<long[]> {

    public final static LongArrayUnserializer instance = new LongArrayUnserializer();

    @Override
    public long[] unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagList) return ReferenceReader.readLongArray(reader);
        if (tag == TagEmpty) return new long[0];
        return super.unserialize(reader, tag, type);
    }

    public long[] read(Reader reader) throws IOException {
        return read(reader, long[].class);
    }
}
