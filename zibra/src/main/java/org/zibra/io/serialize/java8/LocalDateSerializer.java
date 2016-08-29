package org.zibra.io.serialize.java8;

import org.zibra.io.serialize.ReferenceSerializer;
import org.zibra.io.serialize.ValueWriter;
import org.zibra.io.serialize.Writer;
import static org.zibra.io.Tags.TagSemicolon;
import static org.zibra.io.Tags.TagString;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;

public final class LocalDateSerializer extends ReferenceSerializer<LocalDate> {

    public final static LocalDateSerializer instance = new LocalDateSerializer();

    @Override
    public final void serialize(Writer writer, LocalDate date) throws IOException {
        super.serialize(writer, date);
        OutputStream stream = writer.stream;
        int year = date.getYear();
        if (year > 9999 || year < 1) {
            stream.write(TagString);
            ValueWriter.write(stream, date.toString());
        }
        else {
            ValueWriter.writeDate(stream, year, date.getMonthValue(), date.getDayOfMonth());
            stream.write(TagSemicolon);
        }
    }
}
