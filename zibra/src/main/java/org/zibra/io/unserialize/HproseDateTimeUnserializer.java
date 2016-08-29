package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagDate;
import static org.zibra.io.Tags.TagDouble;
import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagInteger;
import static org.zibra.io.Tags.TagLong;
import static org.zibra.io.Tags.TagString;
import static org.zibra.io.Tags.TagTime;
import org.zibra.util.DateTime;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;

public final class HproseDateTimeUnserializer extends BaseUnserializer<DateTime> {

    public final static HproseDateTimeUnserializer instance = new HproseDateTimeUnserializer();

    @Override
    @SuppressWarnings({"deprecation"})
    public DateTime unserialize(Reader reader, int tag, Type type) throws IOException {
        switch (tag) {
            case TagDate: return ReferenceReader.readDateTime(reader);
            case TagTime:  return ReferenceReader.readTime(reader);
            case TagEmpty: return null;
            case TagString: return new DateTime(new Date(ReferenceReader.readString(reader)));
            case TagInteger:
            case TagLong: return new DateTime(new Date(ValueReader.readLong(reader)));
            case TagDouble: return new DateTime(new Date((long)ValueReader.readDouble(reader)));
        }
        if (tag >= '0' && tag <= '9') return new DateTime(new Date(tag - '0'));
        return super.unserialize(reader, tag, type);
    }

    public DateTime read(Reader reader) throws IOException {
        return read(reader, DateTime.class);
    }
}
