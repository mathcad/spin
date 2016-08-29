package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagList;
import java.io.IOException;
import java.lang.reflect.Type;

public final class StringBuilderArrayUnserializer extends BaseUnserializer<StringBuilder[]> {

    public final static StringBuilderArrayUnserializer instance = new StringBuilderArrayUnserializer();

    @Override
    public StringBuilder[] unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagList) return ReferenceReader.readStringBuilderArray(reader);
        if (tag == TagEmpty) return new StringBuilder[0];
        return super.unserialize(reader, tag, type);
    }

    public StringBuilder[] read(Reader reader) throws IOException {
        return read(reader, StringBuilder[].class);
    }
}
