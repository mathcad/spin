package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagSemicolon;
import static org.zibra.io.Tags.TagUTC;
import org.zibra.util.TimeZoneUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.TimeZone;

public final class CalendarSerializer extends ReferenceSerializer<Calendar> {

    public final static CalendarSerializer instance = new CalendarSerializer();

    @Override
    public final void serialize(Writer writer, Calendar calendar) throws IOException {
        super.serialize(writer, calendar);
        TimeZone tz = calendar.getTimeZone();
        if (!(tz.hasSameRules(TimeZoneUtil.DefaultTZ) || tz.hasSameRules(TimeZoneUtil.UTC))) {
            tz = TimeZoneUtil.UTC;
            Calendar c = (Calendar) calendar.clone();
            c.setTimeZone(tz);
            calendar = c;
        }
        OutputStream stream = writer.stream;
        ValueWriter.writeDateOfCalendar(stream, calendar);
        ValueWriter.writeTimeOfCalendar(stream, calendar, true, false);
        stream.write(tz.hasSameRules(TimeZoneUtil.UTC) ? TagUTC : TagSemicolon);
    }
}
