package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagDate;
import static org.zibra.io.Tags.TagDouble;
import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagFalse;
import static org.zibra.io.Tags.TagInteger;
import static org.zibra.io.Tags.TagLong;
import static org.zibra.io.Tags.TagSemicolon;
import static org.zibra.io.Tags.TagString;
import static org.zibra.io.Tags.TagTime;
import static org.zibra.io.Tags.TagTrue;
import static org.zibra.io.Tags.TagUTF8Char;
import java.io.IOException;
import java.lang.reflect.Type;

public class LongObjectUnserializer extends BaseUnserializer<Long> {

    public final static LongObjectUnserializer instance = new LongObjectUnserializer();

    @Override
    public Long unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag >= '0' && tag <= '9') return (long)(tag - '0');
        if (tag == TagInteger || tag == TagLong) return ValueReader.readLong(reader, TagSemicolon);
        switch (tag) {
            case TagDouble: return Double.valueOf(ValueReader.readDouble(reader)).longValue();
            case TagEmpty: return 0L;
            case TagTrue: return 1L;
            case TagFalse: return 0L;
            case TagDate: return ReferenceReader.readDateTime(reader).toLong();
            case TagTime: return ReferenceReader.readTime(reader).toLong();
            case TagUTF8Char: return Long.parseLong(ValueReader.readUTF8Char(reader));
            case TagString: return Long.parseLong(ReferenceReader.readString(reader));
        }
        return super.unserialize(reader, tag, type);
    }

    public Long read(Reader reader) throws IOException {
        return read(reader, Long.class);
    }
}
