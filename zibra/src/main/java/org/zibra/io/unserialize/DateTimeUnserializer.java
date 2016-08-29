package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagDate;
import static org.zibra.io.Tags.TagDouble;
import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagInteger;
import static org.zibra.io.Tags.TagLong;
import static org.zibra.io.Tags.TagString;
import static org.zibra.io.Tags.TagTime;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;

public final class DateTimeUnserializer extends BaseUnserializer<Date> {

    public final static DateTimeUnserializer instance = new DateTimeUnserializer();

    @Override
    @SuppressWarnings({"deprecation"})
    public Date unserialize(Reader reader, int tag, Type type) throws IOException {
        switch (tag) {
            case TagDate: return ReferenceReader.readDateTime(reader).toDateTime();
            case TagTime:  return ReferenceReader.readTime(reader).toDateTime();
            case TagEmpty: return null;
            case TagString: return new Date(ReferenceReader.readString(reader));
            case TagInteger:
            case TagLong: return new Date(ValueReader.readLong(reader));
            case TagDouble: return new Date((long)ValueReader.readDouble(reader));
        }
        if (tag >= '0' && tag <= '9') return new Date(tag - '0');
        return super.unserialize(reader, tag, type);
    }

    public Date read(Reader reader) throws IOException {
        return read(reader, Date.class);
    }
}
