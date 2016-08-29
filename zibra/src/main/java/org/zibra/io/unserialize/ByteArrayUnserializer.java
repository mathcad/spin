package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagBytes;
import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagList;
import static org.zibra.io.Tags.TagString;
import static org.zibra.io.Tags.TagUTF8Char;
import java.io.IOException;
import java.lang.reflect.Type;

public final class ByteArrayUnserializer extends BaseUnserializer<byte[]> {

    public final static ByteArrayUnserializer instance = new ByteArrayUnserializer();

    @Override
    public byte[] unserialize(Reader reader, int tag, Type type) throws IOException {
        switch (tag) {
            case TagEmpty: return new byte[0];
            case TagBytes: return ReferenceReader.readBytes(reader);
            case TagList: return ReferenceReader.readByteArray(reader);
            case TagUTF8Char: return ValueReader.readUTF8Char(reader).getBytes("UTF-8");
            case TagString: return ReferenceReader.readString(reader).getBytes("UTF-8");
        }
        return super.unserialize(reader, tag, type);
    }

    public byte[] read(Reader reader) throws IOException {
        return read(reader, byte[].class);
    }
}
