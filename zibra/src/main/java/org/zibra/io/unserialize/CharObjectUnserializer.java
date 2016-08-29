package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagDouble;
import static org.zibra.io.Tags.TagInteger;
import static org.zibra.io.Tags.TagLong;
import static org.zibra.io.Tags.TagString;
import static org.zibra.io.Tags.TagUTF8Char;
import java.io.IOException;
import java.lang.reflect.Type;

public class CharObjectUnserializer  extends BaseUnserializer<Character> {

    public final static CharObjectUnserializer instance = new CharObjectUnserializer();

    @Override
    public Character unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagUTF8Char) return ValueReader.readChar(reader);
        if (tag >= '0' && tag <= '9') return (char)tag;
        switch (tag) {
            case TagInteger: return (char)ValueReader.readInt(reader);
            case TagLong: return (char)ValueReader.readLong(reader);
            case TagDouble: return (char)Double.valueOf(ValueReader.readDouble(reader)).intValue();
            case TagString: return ReferenceReader.readString(reader).charAt(0);
        }
        return super.unserialize(reader, tag, type);
    }

    public Character read(Reader reader) throws IOException {
        return read(reader, Character.class);
    }
}