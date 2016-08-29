package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagList;
import java.io.IOException;
import java.lang.reflect.Type;

public final class StringBufferArrayUnserializer extends BaseUnserializer<StringBuffer[]> {

    public final static StringBufferArrayUnserializer instance = new StringBufferArrayUnserializer();

    @Override
    public StringBuffer[] unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagList) return ReferenceReader.readStringBufferArray(reader);
        if (tag == TagEmpty) return new StringBuffer[0];
        return super.unserialize(reader, tag, type);
    }

    public StringBuffer[] read(Reader reader) throws IOException {
        return read(reader, StringBuffer[].class);
    }
}
