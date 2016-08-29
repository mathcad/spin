package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagList;
import java.io.IOException;
import java.lang.reflect.Type;

public final class BytesArrayUnserializer extends BaseUnserializer<byte[][]> {

    public final static BytesArrayUnserializer instance = new BytesArrayUnserializer();

    @Override
    public byte[][] unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagList) return ReferenceReader.readBytesArray(reader);
        if (tag == TagEmpty) return new byte[0][];
        return super.unserialize(reader, tag, type);
    }

    public byte[][] read(Reader reader) throws IOException {
        return read(reader, byte[][].class);
    }
}
