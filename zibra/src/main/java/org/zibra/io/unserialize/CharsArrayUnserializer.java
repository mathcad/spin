package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagList;
import java.io.IOException;
import java.lang.reflect.Type;

public final class CharsArrayUnserializer extends BaseUnserializer<char[][]> {

    public final static CharsArrayUnserializer instance = new CharsArrayUnserializer();

    @Override
    public char[][] unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagList) return ReferenceReader.readCharsArray(reader);
        if (tag == TagEmpty) return new char[0][];
        return super.unserialize(reader, tag, type);
    }

    public char[][] read(Reader reader) throws IOException {
        return read(reader, char[][].class);
    }
}
