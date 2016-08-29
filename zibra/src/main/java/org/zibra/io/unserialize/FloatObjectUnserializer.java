package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagDouble;
import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagFalse;
import static org.zibra.io.Tags.TagInfinity;
import static org.zibra.io.Tags.TagInteger;
import static org.zibra.io.Tags.TagLong;
import static org.zibra.io.Tags.TagNaN;
import static org.zibra.io.Tags.TagSemicolon;
import static org.zibra.io.Tags.TagString;
import static org.zibra.io.Tags.TagTrue;
import static org.zibra.io.Tags.TagUTF8Char;
import java.io.IOException;
import java.lang.reflect.Type;

public class FloatObjectUnserializer extends BaseUnserializer<Float> {

    public final static FloatObjectUnserializer instance = new FloatObjectUnserializer();

    @Override
    public Float unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagDouble) return ValueReader.readFloat(reader);
        if (tag >= '0' && tag <= '9') return (float)(tag - '0');
        if (tag == TagInteger) return (float)ValueReader.readInt(reader, TagSemicolon);
        switch (tag) {
            case TagLong: return ValueReader.readLongAsFloat(reader);
            case TagEmpty: return 0.0f;
            case TagTrue: return 1.0f;
            case TagFalse: return 0.0f;
            case TagNaN: return Float.NaN;
            case TagInfinity: return ValueReader.readFloatInfinity(reader);
            case TagUTF8Char: return ValueReader.parseFloat(ValueReader.readUTF8Char(reader));
            case TagString: return ValueReader.parseFloat(ReferenceReader.readString(reader));
        }
        return super.unserialize(reader, tag, type);
    }

    public Float read(Reader reader) throws IOException {
        return read(reader, Float.class);
    }
}
