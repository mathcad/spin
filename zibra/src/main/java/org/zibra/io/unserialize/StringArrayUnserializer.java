package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagList;
import java.io.IOException;
import java.lang.reflect.Type;

public final class StringArrayUnserializer extends BaseUnserializer<String[]> {

    public final static StringArrayUnserializer instance = new StringArrayUnserializer();

    @Override
    public String[] unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagList) return ReferenceReader.readStringArray(reader);
        if (tag == TagEmpty) return new String[0];
        return super.unserialize(reader, tag, type);
    }

    public String[] read(Reader reader) throws IOException {
        return read(reader, String[].class);
    }
}
