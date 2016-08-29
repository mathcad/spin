package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagSemicolon;
import org.zibra.util.DateTime;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;

public final class DateTimeSerializer extends ReferenceSerializer<Date> {

    public final static DateTimeSerializer instance = new DateTimeSerializer();

    @Override
    public final void serialize(Writer writer, Date date) throws IOException {
        super.serialize(writer, date);
        OutputStream stream = writer.stream;
        Calendar calendar = DateTime.toCalendar(date);
        ValueWriter.writeDateOfCalendar(stream, calendar);
        ValueWriter.writeTimeOfCalendar(stream, calendar, true, false);
        stream.write(TagSemicolon);
    }
}
