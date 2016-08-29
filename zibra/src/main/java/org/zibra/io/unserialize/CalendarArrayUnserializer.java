package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagList;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Calendar;

public final class CalendarArrayUnserializer extends BaseUnserializer<Calendar[]> {

    public final static CalendarArrayUnserializer instance = new CalendarArrayUnserializer();

    @Override
    public Calendar[] unserialize(Reader reader, int tag, Type type) throws IOException {
        if (tag == TagList) return ReferenceReader.readCalendarArray(reader);
        if (tag == TagEmpty) return new Calendar[0];
        return super.unserialize(reader, tag, type);
    }

    public Calendar[] read(Reader reader) throws IOException {
        return read(reader, Calendar[].class);
    }
}
