package org.zibra.io.unserialize.java8;

import org.zibra.io.unserialize.BaseUnserializer;
import org.zibra.io.unserialize.Reader;
import org.zibra.io.unserialize.ReferenceReader;
import org.zibra.io.unserialize.ValueReader;
import static org.zibra.io.Tags.TagDate;
import static org.zibra.io.Tags.TagDouble;
import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagInteger;
import static org.zibra.io.Tags.TagLong;
import static org.zibra.io.Tags.TagString;
import static org.zibra.io.Tags.TagTime;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Year;

public final class YearUnserializer extends BaseUnserializer<Year> {

    public final static YearUnserializer instance = new YearUnserializer();

    @Override
    public Year unserialize(Reader reader, int tag, Type type) throws IOException {
        switch (tag) {
            case TagDate: return Year.of(ReferenceReader.readDateTime(reader).year);
            case TagTime:  return Year.of(ReferenceReader.readTime(reader).year);
            case TagEmpty: return null;
            case TagString: return Year.parse(ReferenceReader.readString(reader));
            case TagInteger: return Year.of(ValueReader.readInt(reader));
            case TagLong: return Year.of((int)ValueReader.readLong(reader));
            case TagDouble: return Year.of((int)ValueReader.readDouble(reader));
        }
        if (tag >= '0' && tag <= '9') return Year.of(tag - '0');
        return super.unserialize(reader, tag, type);
    }

    public Year read(Reader reader) throws IOException {
        return read(reader, Year.class);
    }
}
