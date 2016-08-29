package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagList;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Timestamp;

public final class TimestampArrayUnserializer extends BaseUnserializer<Timestamp[]> {

    public final static TimestampArrayUnserializer instance = new TimestampArrayUnserializer();

    @Override
    public Timestamp[] unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagList) return ReferenceReader.readTimestampArray(reader);
        if (tag == TagEmpty) return new Timestamp[0];
        return super.unserialize(reader, tag, type);
    }

    public Timestamp[] read(Reader reader) throws IOException {
        return read(reader, Timestamp[].class);
    }
}
