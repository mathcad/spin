package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagDouble;
import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagFalse;
import static org.zibra.io.Tags.TagInfinity;
import static org.zibra.io.Tags.TagInteger;
import static org.zibra.io.Tags.TagLong;
import static org.zibra.io.Tags.TagNaN;
import static org.zibra.io.Tags.TagString;
import static org.zibra.io.Tags.TagTrue;
import static org.zibra.io.Tags.TagUTF8Char;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;

public class BooleanObjectUnserializer extends BaseUnserializer<Boolean> {

    public final static BooleanObjectUnserializer instance = new BooleanObjectUnserializer();

    @Override
    public Boolean unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagTrue) return true;
        if (tag == TagFalse) return false;
        if (tag == '0') return false;
        if (tag >= '1' && tag <= '9') return true;
        switch (tag) {
            case TagInteger: return ValueReader.readInt(reader) != 0;
            case TagLong: return !(BigInteger.ZERO.equals(ValueReader.readBigInteger(reader)));
            case TagDouble: return ValueReader.readDouble(reader) != 0.0;
            case TagEmpty: return false;
            case TagNaN: return true;
            case TagInfinity: reader.stream.read(); return true;
            case TagUTF8Char: return "\00".indexOf(ValueReader.readChar(reader)) == -1;
            case TagString: return Boolean.parseBoolean(ReferenceReader.readString(reader));
        }
        return super.unserialize(reader, tag, type);
    }

    public Boolean read(Reader reader) throws IOException {
        return read(reader, Boolean.class);
    }
}