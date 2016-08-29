package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagList;
import java.io.IOException;
import java.lang.reflect.Type;

public final class IntArrayUnserializer extends BaseUnserializer<int[]> {

    public final static IntArrayUnserializer instance = new IntArrayUnserializer();

    @Override
    public int[] unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagList) return ReferenceReader.readIntArray(reader);
        if (tag == TagEmpty) return new int[0];
        return super.unserialize(reader, tag, type);
    }

    public int[] read(Reader reader) throws IOException {
        return read(reader, int[].class);
    }
}
