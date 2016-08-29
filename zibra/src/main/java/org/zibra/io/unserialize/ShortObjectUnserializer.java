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

public class ShortObjectUnserializer extends BaseUnserializer<Short> {

    public final static ShortObjectUnserializer instance = new ShortObjectUnserializer();

    @Override
    public Short unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag >= '0' && tag <= '9') return (short)(tag - '0');
        if (tag == TagInteger) return (short)ValueReader.readInt(reader, TagSemicolon);
        switch (tag) {
            case TagLong: return (short)ValueReader.readLong(reader, TagSemicolon);
            case TagDouble: return Double.valueOf(ValueReader.readDouble(reader)).shortValue();
            case TagEmpty: return 0;
            case TagTrue: return 1;
            case TagFalse: return 0;
            case TagUTF8Char: return Short.parseShort(ValueReader.readUTF8Char(reader));
            case TagString: return Short.parseShort(ReferenceReader.readString(reader));
        }
        return super.unserialize(reader, tag, type);
    }

    public Short read(Reader reader) throws IOException {
        return read(reader, Short.class);
    }
}
