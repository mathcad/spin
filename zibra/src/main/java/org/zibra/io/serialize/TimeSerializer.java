package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagSemicolon;
import org.zibra.util.DateTime;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Time;
import java.util.Calendar;

public final class TimeSerializer extends ReferenceSerializer<Time> {

    public final static TimeSerializer instance = new TimeSerializer();

    @Override
    public final void serialize(Writer writer, Time time) throws IOException {
        super.serialize(writer, time);
        OutputStream stream = writer.stream;
        Calendar calendar = DateTime.toCalendar(time);
        ValueWriter.writeTimeOfCalendar(stream, calendar, false, false);
        stream.write(TagSemicolon);
    }
}
