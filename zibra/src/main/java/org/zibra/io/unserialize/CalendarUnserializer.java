package org.zibra.io.unserialize;

import static org.zibra.io.Tags.TagDate;
import static org.zibra.io.Tags.TagDouble;
import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagInteger;
import static org.zibra.io.Tags.TagLong;
import static org.zibra.io.Tags.TagTime;
import org.zibra.io.convert.CalendarConverter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Calendar;

public final class CalendarUnserializer extends BaseUnserializer<Calendar> {

    public final static CalendarUnserializer instance = new CalendarUnserializer();

    @Override
    public Calendar unserialize(Reader reader, int tag, Type type) throws IOException {
        CalendarConverter converter = CalendarConverter.instance;
        switch (tag) {
            case TagDate: return ReferenceReader.readDateTime(reader).toCalendar();
            case TagTime:  return ReferenceReader.readTime(reader).toCalendar();
            case TagEmpty: return null;
            case TagInteger:
            case TagLong: return converter.convertTo(ValueReader.readLong(reader));
            case TagDouble: return converter.convertTo(ValueReader.readDouble(reader));
        }
        if (tag >= '0' && tag <= '9') return converter.convertTo((long)(tag - '0'));
        return super.unserialize(reader, tag, type);
    }

    public Calendar read(Reader reader) throws IOException {
        return read(reader, Calendar.class);
    }
}