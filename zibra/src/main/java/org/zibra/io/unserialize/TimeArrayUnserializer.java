package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagList;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Time;

public final class TimeArrayUnserializer extends BaseUnserializer<Time[]> {

    public final static TimeArrayUnserializer instance = new TimeArrayUnserializer();

    @Override
    public Time[] unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagList) return ReferenceReader.readTimeArray(reader);
        if (tag == TagEmpty) return new Time[0];
        return super.unserialize(reader, tag, type);
    }

    public Time[] read(Reader reader) throws IOException {
        return read(reader, Time[].class);
    }
}
