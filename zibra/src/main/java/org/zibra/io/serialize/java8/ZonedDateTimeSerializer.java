package org.zibra.io.serialize.java8;

import static org.zibra.io.Tags.TagString;
import static org.zibra.io.Tags.TagUTC;
import org.zibra.io.serialize.ReferenceSerializer;
import org.zibra.io.serialize.ValueWriter;
import org.zibra.io.serialize.Writer;
import java.io.IOException;
import java.io.OutputStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public final class ZonedDateTimeSerializer extends ReferenceSerializer<ZonedDateTime> {

    public final static ZonedDateTimeSerializer instance = new ZonedDateTimeSerializer();

    @Override
    public final void serialize(Writer writer, ZonedDateTime datetime) throws IOException {
        super.serialize(writer, datetime);
        OutputStream stream = writer.stream;
        if (!(datetime.getOffset().equals(ZoneOffset.UTC))) {
            stream.write(TagString);
            ValueWriter.write(stream, datetime.toString());
        }
        else {
            int year = datetime.getYear();
            if (year > 9999 || year < 1) {
                stream.write(TagString);
                ValueWriter.write(stream, datetime.toString());
            }
            else {
                ValueWriter.writeDate(stream, year, datetime.getMonthValue(), datetime.getDayOfMonth());
                ValueWriter.writeTime(stream, datetime.getHour(), datetime.getMinute(), datetime.getSecond(), 0, false, true);
                ValueWriter.writeNano(stream, datetime.getNano());
                stream.write(TagUTC);
            }
        }
    }
}
