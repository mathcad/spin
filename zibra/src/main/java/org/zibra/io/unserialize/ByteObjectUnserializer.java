package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagDouble;
import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagFalse;
import static org.zibra.io.Tags.TagInteger;
import static org.zibra.io.Tags.TagLong;
import static org.zibra.io.Tags.TagSemicolon;
import static org.zibra.io.Tags.TagString;
import static org.zibra.io.Tags.TagTrue;
import static org.zibra.io.Tags.TagUTF8Char;
import java.io.IOException;
import java.lang.reflect.Type;

public class ByteObjectUnserializer  extends BaseUnserializer<Byte> {

    public final static ByteObjectUnserializer instance = new ByteObjectUnserializer();

    @Override
    public Byte unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag >= '0' && tag <= '9') return (byte)(tag - '0');
        if (tag == TagInteger) return (byte)ValueReader.readInt(reader, TagSemicolon);
        switch (tag) {
            case TagLong: return (byte)ValueReader.readLong(reader, TagSemicolon);
            case TagDouble: return Double.valueOf(ValueReader.readDouble(reader)).byteValue();
            case TagEmpty: return 0;
            case TagTrue: return 1;
            case TagFalse: return 0;
            case TagUTF8Char: return Byte.parseByte(ValueReader.readUTF8Char(reader));
            case TagString: return Byte.parseByte(ReferenceReader.readString(reader));
        }
        return super.unserialize(reader, tag, type);
    }

    public Byte read(Reader reader) throws IOException {
        return read(reader, Byte.class);
    }
}
