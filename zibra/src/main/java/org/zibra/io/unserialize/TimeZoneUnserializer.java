package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagString;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.TimeZone;

public final class TimeZoneUnserializer extends BaseUnserializer<TimeZone> {

    public final static TimeZoneUnserializer instance = new TimeZoneUnserializer();

    @Override
    public TimeZone unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagString) return TimeZone.getTimeZone(ReferenceReader.readString(reader));
        if (tag == TagEmpty) return null;
        return super.unserialize(reader, tag, type);
    }

    public TimeZone read(Reader reader) throws IOException {
        return read(reader, TimeZone.class);
    }
}
