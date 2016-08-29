package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagList;
import java.io.IOException;
import java.lang.reflect.Type;

public final class BooleanArrayUnserializer extends BaseUnserializer<boolean[]> {

    public final static BooleanArrayUnserializer instance = new BooleanArrayUnserializer();

    @Override
    public boolean[] unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagList) return ReferenceReader.readBooleanArray(reader);
        if (tag == TagEmpty) return new boolean[0];
        return super.unserialize(reader, tag, type);
    }

    public boolean[] read(Reader reader) throws IOException {
       return read(reader, boolean[].class);
    }
}
