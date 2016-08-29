package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagList;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Date;

public final class DateArrayUnserializer extends BaseUnserializer<Date[]> {

    public final static DateArrayUnserializer instance = new DateArrayUnserializer();

    @Override
    public Date[] unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagList) return ReferenceReader.readDateArray(reader);
        if (tag == TagEmpty) return new Date[0];
        return super.unserialize(reader, tag, type);
    }

    public Date[] read(Reader reader) throws IOException {
        return read(reader, Date[].class);
    }
}
