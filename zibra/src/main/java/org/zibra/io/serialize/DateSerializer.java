package org.zibra.io.serialize;

import static org.zibra.io.Tags.TagSemicolon;
import org.zibra.util.DateTime;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Date;
import java.util.Calendar;

public final class DateSerializer extends ReferenceSerializer<Date> {

    public final static DateSerializer instance = new DateSerializer();

    @Override
    public final void serialize(Writer writer, Date date) throws IOException {
        super.serialize(writer, date);
        OutputStream stream = writer.stream;
        Calendar calendar = DateTime.toCalendar(date);
        ValueWriter.writeDateOfCalendar(stream, calendar);
        stream.write(TagSemicolon);
    }
}
