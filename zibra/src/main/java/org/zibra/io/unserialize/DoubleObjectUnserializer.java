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

public class DoubleObjectUnserializer extends BaseUnserializer<Double> {

    public final static DoubleObjectUnserializer instance = new DoubleObjectUnserializer();

    @Override
    public Double unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagDouble) return ValueReader.readDouble(reader);
        if (tag >= '0' && tag <= '9') return (double)(tag - '0');
        if (tag == TagInteger) return (double)ValueReader.readInt(reader, TagSemicolon);
        switch (tag) {
            case TagLong: return ValueReader.readLongAsDouble(reader);
            case TagEmpty: return 0.0;
            case TagTrue: return 1.0;
            case TagFalse: return 0.0;
            case TagNaN: return Double.NaN;
            case TagInfinity: return ValueReader.readInfinity(reader);
            case TagUTF8Char: return ValueReader.parseDouble(ValueReader.readUTF8Char(reader));
            case TagString: return ValueReader.parseDouble(ReferenceReader.readString(reader));
        }
        return super.unserialize(reader, tag, type);
    }

    public Double read(Reader reader) throws IOException {
        return read(reader, Double.class);
    }
}
